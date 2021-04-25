# elasticjob

### 此分支为 基于Spring 和 ElasticJob 框架实现的分布式定时任务组件

	
1、使用时需要在启动类上加上 @EnableElasticJob 注解
   
2、具体的定时任务，需要在类上加上 @ElasticJobComponent 注解，可替代
   @Component 注解
   
3、具体的定时任务方法上，使用 @Schedule 注解配置，使用方式与 Spring
   的 @Schedule注解基本一致
   
4、配置文件需要配置注册中心列表，多个用英文逗号隔开：

zookeeper:
  servicelists: 127.0.0.1:1231,127.0.0.1:4567

5、自定义分片策略的话，需要实现 JobShardingStrategy 接口
