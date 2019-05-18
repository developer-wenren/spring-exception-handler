# 掌握 Spring 之异常处理

![公众号](http://ww1.sinaimg.cn/large/006tNc79ly1g35nzax5gvj30p00dwtaq.jpg)

## 前言

这次我们学习 Spring 的异常处理，作为一个 Spring 为基础框架的 Web 程序，如果不对程序中出现的异常进行适当的处理比如异常信息友好化，记录异常日志等等，直接将异常信息返回给客户端展示给用户，对用户体验有不好的影响。所以本篇文章主要探讨通过 Spring 进行统一异常处理的几种方式实现，以更优雅的方式捕获程序发生的异常信息并进行适当的处理响应给客户端。

本文主要内容涉及如下：

- `HandlerExceptionResolver` 扩展
- `@ExceptionHandler` 和 `@ControllerAdvice` 使用
- `ResponseEntityExceptionHandler` 扩展
- `ResponseStatusException` 使用
- Spring Boot `ErrorController` 扩展

> 示例项目：
>
> - spring-exception-handler: https://github.com/wrcj12138aaa/spring-exception-handler
>
> 环境支持：
>
> - JDK 8
> - SpringBoot 2.1.4
> - Maven 3.6.0

## 正文

Spring 框架的异常处理提供了许多种方式，在 Spring 3.2 之前主要有两种处理方式：扩展 `HandlerExceptionResolver` 和 使用注解 [@ExceptionHandler](https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/web.html#mvc-ann-exceptionhandler)，Spring 3.2 之后提供了更丰富的处理方式。

### HandlerExceptionResolver 扩展

`HandlerExceptionResolver` 是一个处理 Web 程序发生异常时的接口，接口方法如下：

```java
@Nullable
ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex);
```

从返回值类型 `ModelAndView` 可以看出，这个属于 Spring MVC 框架中的接口，实现此方法就可以对捕获的异常进行解析处理，然后根据自身需要返回 `ModelAndView` 对象，以 JSON 数据或者页面形式响应客户端请求。

首先来看下 `HandlerExceptionResolver` 类层次体系，Spring 提供了 4 个实现类，下面根据这些类做了简单的描述。

![HandlerExceptionResolver 类体系](http://ww4.sinaimg.cn/large/006tNc79ly1g35v0q51y0j31560a10u1.jpg)

| HandlerExceptionResolver            | 描述                                                                                                |
| ----------------------------------- | --------------------------------------------------------------------------------------------------- |
| `SimpleMappingExceptionResolver`    | 映射异常类到指定视图，一般用于展现异常发生时的错误页面                                              |
| `DefaultHandlerExceptionResolver`   | `HandlerExceptionResolver` 的默认实现，处理 Spring MVC 异常                                         |
| `ResponseStatusExceptionResolver`   | 处理带有 `@ResponseStatus` 注解的异常，将注解上对应的值转换为 HTTP 状态码，一般放于自定义的异常类上 |
| `ExceptionHandlerExceptionResolver` | 处理带有 `@ExceptionHandler`注解的异常                                                              |

当我们需要实现自定义的 `HandlerExceptionResolver`时，只要通过继承它的抽象类 `AbstractHandlerExceptionResolver`，覆写 `doResolveException`方法就可以了。

下方的示例代码处理了程序中发生的 `IllegalArgumentException` 异常时的情况，并通过 `MappingJackson2JsonView` 对象返回客户端一个 JSON 数据对象。如果不是 `IllegalArgumentException`异常，返回 `null` 表示让其他异常处理器进行处理，这里由于异常处理链机制，如果不处理异常，就会由 Web 容器将异常返回给客户端。

```java
@Component
public class RestResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (ex instanceof IllegalArgumentException) {
                ModelAndView modelAndView = new ModelAndView();
                Map<String, String> maps = new HashMap<>();
                maps.put("code", "400");
                maps.put("message", ex.getClass().getName());
                maps.put("data", null);
                MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
                mappingJackson2JsonView.setAttributesMap(maps);
                modelAndView.setView(mappingJackson2JsonView);
                return modelAndView;
            }
        } catch (Exception handlerException) {
            logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
        }
        return null;
    }

}
```

我们使用 Postman 工具模拟请求项目的 API 接口 `/exception1` 来导致异常的触发，正常可以看到如下效果：

![image-20190518131151510](http://ww2.sinaimg.cn/large/006tNc79ly1g35e6h2qntj30f80bvq3f.jpg)

### @ExceptionHandler

接下来我们看下 `@ExceptionHandler` 的用法，这个注解通常定义在某个控制器下的方法里，表明处理该控制器出现的指定异常, 如下代码所示：

```java
@RestController
public class RestApiController {
    //...

    @ExceptionHandler({IllegalStateException.class})
    public ModelAndView handleIllegalStateException(IllegalStateException ex) {
        System.out.println("非法状态异常出现,需要处理 " + ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        Map<String, String> maps = new HashMap<>();
        maps.put("data", null);
        maps.put("message", ex.getClass().getName());
        maps.put("code", "400");
        MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
        mappingJackson2JsonView.setAttributesMap(maps);
        modelAndView.setView(mappingJackson2JsonView);
        return modelAndView;
    }
}
```

> `@ExceptionHandler` 可以设置多个需要捕获处理的异常类型，也可以不填默认为所有异常类,更多信息可以查看 [mvc-ann-exceptionhandler][mvc-ann-exceptionhandler]

然后使用 Postman 工具模拟请求项目的 API 接口 `/exception2` 来触发异常，看下响应数据：

![image-20190518134744575](http://ww4.sinaimg.cn/large/006tNc79ly1g35f7qidfcj30gn0c0aaj.jpg)

这样方式使用 `@ExceptionHandler` 存在一个缺陷，就是只会针对当前控制器下的异常处理，若需要实现全局控制器的异常处理，还需要配合注解 `@ControllerAdvice` 一起使用，接下来就介绍这个处理方式。

### @ControllerAdvice

Spring 3.2 引入了一种新注解 `@ControllerAdvice`，用于将所有控制器中异常的处理放在一处进行，将指定一个类作为全局异常处理器，用 `@ExceptionHandler` 注解标注的方法去处理异常，具体示例代码如下：

```java
@ControllerAdvice
public class NormalExceptionHandler {
    @ExceptionHandler()
    public ResponseEntity handleException(Exception e) {
        System.out.println("NormalExceptionHandler handle exception");
        return ResponseEntity.ok(new Result<>(400, e.getMessage(), null));
    }
}
```

> 代码中的 Result 对象只是一个数据传输对象 (DTO)，便于返回客户端统一格式的数据。

再来看下使用 Postman 工具模拟请求 API 接口 `/exception3` 响应的数据，见下图。

![image-20190518144403940](http://ww3.sinaimg.cn/large/006tNc79ly1g35guihk8ej30bk0b5mxh.jpg)

还有一个注解 `@RestControllerAdvice` 跟 `@ControllerAdvice` 很相似，其实就是 `@ControllerAdvice` 与 `@ResponseBody`注解的组合，效果就是异常处理方法返回的对象，直接就会被序列化成 JSON 数据给客户端，使用方式如下：

```java
@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler({ArithmeticException.class})
    public Result handlerException(Exception e) {
        return new Result<>(400, e.getMessage(), null);
    }
}
```

这个注解是在 Spring 4.3 版本引入的，主要就是便于针对 REST 请求异常时直接返回 JSON 格式的数据，而不使用 `ResponseEntity` 对象方式传递数据。

`@ControllerAdvice` 默认拦截所有控制器中发生的异常，当然也可以限定范围，限定方式有限定注解，包名等，具体示例如下：

```java
// Target all Controllers annotated with @RestController
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// Target all Controllers within specific packages
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// Target all Controllers assignable to specific classes
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```

对于 全局 `@ExceptionHandler` 方法处理的描述，官方文档还有额外的备注如下：

> Global `@ExceptionHandler` methods (from a `@ControllerAdvice`) are applied _after_ local ones (from the `@Controller`).

这表明了异常处理也存在优先级，先交给当前控制器内的 `@ExceptionHandler`方法处理，若未处理再由全局的`@ExceptionHandler` 方法处理。

### ResponseEntityExceptionHandler 扩展

`ResponseEntityExceptionHandler` 类是主要针对 Spring MVC 所抛出异常的处理类，比如 405 请求，400 请求等，都默认由 `ResponseEntityExceptionHandler`处理，我们可以过继承这个类覆写它的方法，来实现特定请求异常的处理。比如下面代码实现对 405 请求异常的响应处理。

```java
@@ControllerAdvice
public class CustomWebResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        switch (status) {
            case METHOD_NOT_ALLOWED:
                return getMethodNotAllowedResponse(request);
            default:
                return ResponseEntity.ok(new Result<>(status.value(), status.getReasonPhrase(), null));
        }
    }

    public ResponseEntity getMethodNotAllowedResponse(WebRequest request) {
        String uri = "";
        if (request instanceof ServletWebRequest) {
            uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        Result<Object> result = new Result<>();
        result.setCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        result.setMessage(uri + " 请求方式不正确");
        return ResponseEntity.ok(result);
    }
}
```

通过这样的方式，我们尝试发送 GET 请求给 API 接口`/hello`,会有如下返回信息：

![image-20190518162624412](http://ww4.sinaimg.cn/large/006tNc79ly1g35jsrjw5jj30e50c3jru.jpg)

当时 `ResponseEntityExceptionHandler` 也存在局限性，目前支持的 SpringMVC 标准异常只有下面 15 种异常类型:

- `HttpRequestMethodNotSupportedException`
- `HttpMediaTypeNotSupportedException`
- `HttpMediaTypeNotAcceptableException`
- `MissingPathVariableException`
- `MissingServletRequestParameterException`
- `ServletRequestBindingException`
- `ConversionNotSupportedException`
- `TypeMismatchException`
- `HttpMessageNotReadableException`
- `HttpMessageNotWritableException`
- `MethodArgumentNotValidException`
- `MissingServletRequestPartException`
- `BindException`
- `NoHandlerFoundException`
- `AsyncRequestTimeoutException`

### ResponseStatusException

`ResponseStatusException`类是在 Spring 5.0 引入，关联 HTTP 状态码和可选的原因，我们直接就可以在请求方法中构建这个异常对象进行返回，使用起来十分简单：

```java
@GetMapping("/exception4")
public ResponseEntity<String> exception4(String param) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源未找到");
}
```

使用这种方式虽然能直接返回响应码和具体原因，但是没有统一处理异常的效果，通常配合 `@ControllerAdvice` 一起组合使用。

### Spring Boot ErrorController

`ErrorController` 是 Spring Boot 2.0 引入接口，基于此的实现类 `org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController` 为我们提供了一种通用的方式进行错误处理, 下面是这个实现类的关键方法：

```java
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request,
        HttpServletResponse response) {
    HttpStatus status = getStatus(request);
    Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(
            request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
    response.setStatus(status.value());
    ModelAndView modelAndView = resolveErrorView(request, response, status, model);
    return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
}

@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request,
            isIncludeStackTrace(request, MediaType.ALL));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<>(body, status);
}
```

可以从这两个方法看出针对错误请求，`BasicErrorController` 提供了两种数据形式的返回，一种是 HTML 页面，一种是 JSON 数据；如果我们直接使用浏览器访问接口的话见到的就是 `errorHtml`方法返回的 HTML 页面数据，它们的区别就在于请求时 Header 里 Accept 值的不同。

![image-20190518170154527](http://ww3.sinaimg.cn/large/006tNc79ly1g35ktpalx1j313n0doq53.jpg)

另外，Spring Boot 提供统一错误信息处理，是允许关闭的，只要在配置文件 `application.properties` 设置 `server.error.whitelabel.enabled` 为 `false`即可。

```properties
server.error.whitelabel.enabled=false
```

当然我们也可以基于此进行扩展，比如实现一个自定义的错误控制器，继承 `BasicErrorController`,编写自己的错误展示逻辑和内容，比如下面代码：

```java
@Component
public class CustomErrorController extends BasicErrorController {

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes, new ErrorProperties());
    }

    @RequestMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Map<String, Object>> xmlError(HttpServletRequest request, HttpStatus status) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", status.value());
        map.put("message", status.getReasonPhrase());
        return ResponseEntity.ok(map);
    }
}
```

实现的 `CustomErrorController` 针对请求时 Aceept 为 `application/xml`的发生的异常都统一以 XML 格式进行返回，如图：

![image-20190518171944860](http://ww3.sinaimg.cn/large/006tNc79ly1g35lc9jwkyj30er0ebwf1.jpg)

> 注意: Spring Boot 默认不支持数据进行 XML 格式的转换，POM 文件需要额外添加依赖库：
>
> ```xml
> <dependency>
>       <groupId>com.fasterxml.jackson.dataformat</groupId>
>       <artifactId>jackson-dataformat-xml</artifactId>
> </dependency>
> ```

## 结语

本文我们主要学习了 Spring 框架 5 种异常处理的方式以及 Spring Boot 的通用异常处理行为，形式多样，但具体情况需要具体定制，为了保证程序的健壮性和便于快速定位请求出现的异常问题，我们必须为程序提供统一的异常处理方式，也在平时的项目里使用起来吧。

如果读完觉得有收获的话，欢迎点【好看】，点击文章头图，扫码关注【闻人的技术博客】😄😄😄。

## 参考

- Spring Boot 中 Web 应用的统一异常处理 : http://blog.didispace.com/springbootexception
- Error Handling for REST with Spring : https://www.baeldung.com/exception-handling-for-rest-with-spring
- Spring REST Service Exception Handling https://dzone.com/articles/spring-rest-service-exception-handling-1
- mvc-ann-exceptionhandler：https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/web.html#mvc-ann-exceptionhandler
- spring-boot-return-json-and-xml-from-controllers: https://stackoverflow.com/questions/27790998/spring-boot-return-json-and-xml-from-controllers
- Spring Web MVC Exceptions : https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/web.html#mvc-exceptionhandlers

[mvc-ann-exceptionhandler]: https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/web.html#mvc-ann-exceptionhandler

