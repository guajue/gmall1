package com.guajue.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.guajue.gmall.bean.SkuLsInfo;
import com.guajue.gmall.bean.SkuLsParams;
import com.guajue.gmall.bean.SkuLsResult;
import com.guajue.gmall.service.ListService;
import com.guajue.gmall.serviceutil.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.Build;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {


    @Autowired
    private JestClient jestClient;
    
    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        // 保存数据
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams)  {

        //获得查询JSON串
        String search = makeQueryStringForSearch(skuLsParams);

        //建立查询，并获得查询结果
        Search build = new Search.Builder(search).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null;

        try {
            searchResult = jestClient.execute(build);
        }catch (IOException e){
            e.printStackTrace();
        }

        return  makeResultForSearch(skuLsParams, searchResult);
    }

    @Override
    public void incrHotScore(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        //加入ES的次数
        int  count = 10;
        Double hotScore = jedis.zincrby("hotScore", 1, "SkuId:" + skuId);
        if(hotScore%count == 0 ){
            updateHotScore(skuId,  Math.round(hotScore));
        }

    }

    /**
     * 将热度更新至ES
     * @param skuId
     * @param hotScore
     */
    private void updateHotScore(String skuId,Long hotScore){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update build = new Update.Builder(updateJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将SkuLsParams变成查询的字符串
     * @param skuLsParams
     * @return
     */
    public  String makeQueryStringForSearch(SkuLsParams skuLsParams){

        //创建查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //关键字查询并高亮
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {

            //关键字查询
            MatchQueryBuilder ma = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(ma);

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);

        }

        //根据三级分类ID查询
        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);

        }

        //根据属性查询
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (String str:
            skuLsParams.getValueId()) {
                TermQueryBuilder tb = new TermQueryBuilder("skuAttrValueList.valueId", str);
                boolQueryBuilder.filter(tb);
            }
        }

        //建立查询
        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        int from  =(skuLsParams.getPageNo()-1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //热度排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        //获得查询字符串
        String query = searchSourceBuilder.toString();
        System.out.println(query);

        return query;
    }


    /**
     * 处理es返回结果
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams,SearchResult searchResult){

        //创建返回对象
        SkuLsResult skuLsResult=new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList=new ArrayList<>(skuLsParams.getPageSize());

        //获取SKU列表
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);

        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;

            //把带有高亮的标签替换到SkuLsInfo
            if(hit.highlight != null && hit.highlight.size() > 0) {

                List<String> list = hit.highlight.get("skuName");
                String s = list.get(0);
                skuLsInfo.setSkuName(s);

            }
            skuLsInfoList.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        skuLsResult.setTotal(searchResult.getTotal());

        //取出总记录数计算总页数
        long totalPage = searchResult.getTotal()%skuLsParams.getPageSize() == 0 ? searchResult.getTotal()/skuLsParams.getPageSize() :searchResult.getTotal()/skuLsParams.getPageSize() + 1;
        skuLsResult.setTotalPages(totalPage);

        //取出涉及的属性值ID
        ArrayList<String> attrValueIdList = new ArrayList<>();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        if(groupby_attr != null) {
            List<TermsAggregation.Entry> groupby_attrBuckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry t:
                    groupby_attrBuckets) {
                attrValueIdList.add(t.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }

        return skuLsResult;
    }
}

