package com.atguigu.ssyx.search.repository;

import com.atguigu.ssyx.model.search.SkuEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//泛型第一个参数是操作的实体类，第二个是主键类型
public interface SkuRepository extends ElasticsearchRepository<SkuEs,Long> {
}
