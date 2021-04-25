# apollo

### 此分支为 基于Spring Cloud 和 Apollo 框架的动态刷新配置

	
1、使用时需要在启动类上加上 @EnableApolloConfig 注解，并配置好对应
   的配置文件
   
2、属性注入时，可使用 Spring 的 @Value 注解注入属性，如果需要支持动
   态刷新，还需要在该类上加上 @RefreshScope 注解，可参考 Spring Cloud