# redis

### 此分支为 基于springboot实现的 redis相关工具类

	
1、当 @Cache 注解和 @Lock 注解都在同一个方法上时，会先执行 @Cache的
   内容，然后执行 @Lock 的内容
   
2、当需要同时用到 @Cache 注解和 @Lock 注解的时候，可通过注入的方式，
   或者调用 AopContext 来控制执行顺序
   
3、@Cache注解里面的 key 和 hashKey 是 SpEL 表达式的写法，其中包含两
   个默认的对象：#this（AOP的代理对象）和 #target（被代理对象，即连
   接点所在的目标对象），调用时请注意区别