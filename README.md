# druid

### 此分支为 基于springboot实现的 druid相关数据源配置

#### 解释说明

1、主从切换的原理是使用 AOP对指定方法或注解进行切面处理，默认为 @Master注解、@Slave注解、@Transactional注解
   
2、目前支持一主多从的结构，从库为空时默认使用主库，多从库使用策略为轮询

#### 快速使用

1、在 AppApplication启动类中，开启 @EnableDruidDynamicDataSource，并且
   @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})，禁掉这个配置，防止加载配置文件中的 
   spring.datasource.*，以便配置多数据源
   
2、在配置文件中切换数据源配置，如下：
   spring: 
     datasource:
       type: com.alibaba.druid.pool.DruidDataSource
       druid:
         driver-class-name: com.mysql.jdbc.Driver
         master:
           url: jdbc:mysql://127.0.0.1:3306/table?characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true
           username: 自己填
           password: 自己填
         slave:
           url: jdbc:mysql://127.0.0.1:3306/table?characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true
           username: 自己填
           password: 自己填
   \#    初始化建立物理连接的个数
         initial-size: 10
   \#      最小连接池个数
         min-idle: 5
   \      最大连接池个数
         max-active: 20
   \#      获取连接时的最大等待时间，毫秒
         max-wait: 30000
   \#      配置间隔多久才进行一次检测，检测需要关闭的空闲连接，毫秒
         time-between-eviction-runs-millis: 30000
   \#      配置一个连接在连接池中的最小存活时间，毫秒
         min-evictable-idle-time-millis: 30000
   \#      用来检测连接是否有效的sql，必须是一个查询语句
   \#      MySQL为 select 'x'
   \#      Oracle为 select 1 from dual
         validation-query: select 'x'
   \#      申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
         test-while-idle: true
   \#      申请连接时会执行validationQuery检测连接是否有效,开启会降低性能,默认为true
         test-on-borrow: false
   \#      归还连接时会执行validationQuery检测连接是否有效,开启会降低性能,默认为true
         test-on-return: false
   \#      是否缓存preparedStatement,mysql5.5+建议开启
         pool-prepared-statements: true
   \#      当值大于0时poolPreparedStatements会自动修改为true
         max-pool-prepared-statement-per-connection-size: 20
   \#      配置监控统计的拦截filters，去掉后监控界面sql无法统计，wall用于防火墙
         filters: stat,wall,log4j
   \#      通过connectProperties属性来打开mergeSql功能；慢SQL记录
         connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500
   \#      合并多个DruidDataSource的监控数据
         use-global-data-source-stat: true
   \#      监控统计过滤配置
         web-stat-filter:
           url-pattern: /*
   \#        过滤这些请求不统计
           exclusions: '*.js,*.jpg,*.png,*.gif,*.ico,*.css,/druid/*'
   \#      监控页面配置
         stat-view-servlet:
   \#        允许的ip白名单
           allow: 127.0.0.1
   \#        进入页面的url
           url-pattern: /druid/*
   \#        禁止页面上的重置
           reset-enable: false
   \#        登录账号密码
           login-username: 自己填
           login-password: 自己填

3、配置你自己的 MybatisConfig的 DataSource bean，主库的 DataSource需要加上 @Primary注解，动态数据源配置会自动装配主从数据源，
   并且根据 @Primary注解的 bean区分主从，SqlSessionFactory注入 DataSource的时候，注入一个 DynamicDataSource dataSource，
   即为动态数据源

4、如果有需要自己定义需要主从切换的方法，自己编写 AOP类，继承 DynamicDataSourceAOP类，重写 slavePointCut方法和
   masterPointCut 方法

5、然后其他配置正常配置就可以了，项目启动后，通过浏览器，访问： 127.0.0.1:你暴露的端口/项目名/druid/login.html 访问监控页面，
   使用配置的用户名和密码，如果需要做加密操作，自己去百度
                