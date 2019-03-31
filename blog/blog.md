## [ActiveMQ,RabbitMQ,KAFKA] 설정으로 Springboot의 Auto configuration 살펴보기

스프링부트에 "org.springframework.boot:spring-boot-starter-activemq"와 같은 의존성을 추가하고
실행하면 아래와 같이 activemq 관련 빈이 자동으로 생성된 것을 확인할 수 있습니다.

> main class

```aidl
public class RpcQueueApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RpcQueueApplication.class, args);
        displayBeans(ctx);
    }

    private static void displayBeans(ConfigurableApplicationContext ctx) {
        String[] beanNames = ctx.getBeanDefinitionNames();
        logger.info("Bean names : ", Arrays.toString(beanNames));
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("activemq")) {
                logger.info("activemq bean : {}", beanName);
            }
        }
    }
}
```  

> output

```aidl
2019-03-26 21:36:31.394  INFO 3448 --- [  restartedMain] server.RpcQueueApplication               : activemq bean : org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryConfiguration$SimpleConnectionFactoryConfiguration
2019-03-26 21:36:31.395  INFO 3448 --- [  restartedMain] server.RpcQueueApplication               : activemq bean : org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryConfiguration
2019-03-26 21:36:31.395  INFO 3448 --- [  restartedMain] server.RpcQueueApplication               : activemq bean : org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration
2019-03-26 21:36:31.395  INFO 3448 --- [  restartedMain] server.RpcQueueApplication               : activemq bean : spring.activemq-org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties
```  

이유는 @SpringBootApplication을 살펴보면 @EnableAutoConfiguration이 있는 것을 확인할 수 있음  

> @SpringBootApplication  

```aidl
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
  ...
}
```  

또한 org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration 클래스를 살펴보면  
아래와 AutoConfigureBefore, After, Import 등등을 확인할 수 있습니다.  


```aidl
@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class })
@ConditionalOnClass({ ConnectionFactory.class, ActiveMQConnectionFactory.class })
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties({ ActiveMQProperties.class, JmsProperties.class })
@Import({ ActiveMQXAConnectionFactoryConfiguration.class,
		ActiveMQConnectionFactoryConfiguration.class })
public class ActiveMQAutoConfiguration {
}
```  

## @SpringBootApplication에서 exclude() 를 이용하여 자동으로 빈이 생성되지 못하게 하기

```aidl
@SpringBootApplication(exclude = {
    // display auto configs about messages
    org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration.class,
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class,
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
public class RpcQueueApplication {

    public static void main(String[] args) {
      ...
    }
}
```  

위와 같이 AutoConfiguration들을 @SpringBootApplication 어노테이션에서 제외시키면 자동으로 빈이 생성되는 것을  
막을 수 있습니다.

---  

## Rpc type에 따라 설정하기  

아래와 같이 rpc.enable 와 rpc.type에 따라 AcitveMQ, RabbitMQ, Kafka를 사용하도록 설정하겠습니다.  

```$xslt
rpc:  
  enabled: true
  type: rabbitmq
```  

먼저 rpc.enabled에 따라 RpcConfiguration 을 enable / disable 하도록 설정

```$xslt
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnProperty(name = "rpc.enabled", havingValue = "true")
public class RpcConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("RpcConfiguration is enabled");
    }
}
```  

두번째로 org.springframework.context.annotation.Condition을 이용하여 rpc.type 값에 따라 설정하기

```$xslt
public class RpcTypeEnabledCondition {

    public static class ActiveMQEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "activemq".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }

    public static class RabbitMQEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "rabbitmq".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }

    public static class KafkaEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "kafka".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }
}
``` 

마지막으로 AcitveMQConfiguration, RabbitMQConfiguration, KafkaConfiguration 설정하기
- @ConditionalOnBean은 해당 빈이 존재하면 빈을 등록
- @Conditional은 위에서 Condition 인터페이스를 구현하여 특정 조건부로 빈이 등록되도록 설정
- @Import는 해당 Auto configuration 활성

```$xslt
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = ActiveMQEnabledCondition.class)
@Import({ActiveMQAutoConfiguration.class})
public class ActiveMQConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable activemq configuration");
    }
}
```

```$xslt
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = RabbitMQEnabledCondition.class)
@Import({RabbitAutoConfiguration.class})
public class RabbitMQConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable rabbitmq configuration");
    }
}
```  

```$xslt
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = KafkaEnabledCondition.class)
@Import({KafkaAutoConfiguration.class})                
public class KafkaConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable kafka configuration");
    }
}
```



  



 

