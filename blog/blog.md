## Spring boot의 Autoconfiguration 

스프링부트에 "org.springframework.boot:spring-boot-starter-activemq"와 같은 의존성을 추가하고
실행하면 아래와 같이 activemq 관련 빈이 자동으로 생성된 것을 확인할 수 있음

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

또한 org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration 클래스를 살펴보면 아래와 같이 이루어져 있음  


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

## Custom auto configuration   

@SpringBootApplication에서 exclude() 를 이용하여 자동으로 빈이 생성되지 못하게 하기

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

  



 

