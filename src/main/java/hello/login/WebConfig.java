package hello.login;

import hello.login.web.argumentresolver.LoginMemberArgumentResolver;
import hello.login.web.filter.LogFilter;
import hello.login.web.filter.LoginCheckFilter;
import hello.login.web.interceptor.LogInterceptor;
import hello.login.web.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

//logfilter쓸 수 있게 등록해주는 클래스
//LogInterceptor를 등록하려면 WebMvcConfigurer 상속받아 오버라이딩 해야함
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //LoginMemberArgumentResolver 등록,
    //이렇게 등록해야 HomeController homeLoginV3ArgumentResolver @login 쓸 수 있음
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    //LogInterceptor 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LogInterceptor())
                //1순위, 인터셉터의 호출 순서를 지정한다. 낮을 수록 먼저 호출

                .order(1)
                //모든 경로 /*가 아니라 /**
                //인터셉터를 적용하거나 하지 않을 부분은 addPathPatterns 와 excludePathPatterns 에 작성하면
                //된다. 기본적으로 모든 경로에 해당 인터셉터를 적용하되 ( /** ), 홈( / ), 회원가입( /members/add ),
                //로그인( /login ), 리소스 조회( /css/** ), 오류( /error )와 같은 부분은
                // 로그인 체크 인터셉터를 적용하지 않는다. 서블릿 필터와 비교해보면 매우 편리한 것을 알 수 있다.
                .addPathPatterns("/**")
                //이 경로는 인터셉터 적용하지마라, whitelist 만들지 않고 여기서
                .excludePathPatterns("/css/**", "/*.ico", "/error");

        //LoginCheckInterceptor() 등록
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/members/add", "/login", "/logout",
                        "/css/**", "/*.ico", "/error");
    }

    //스프링부트로 사용할때 필터 등록할때 이렇게
    //스프링부트가 was를 띄우기 때문에 필터를 같이 넣어줌

    //@ServletComponentScan @WebFilter(filterName = "logFilter", urlPatterns = "/*") 로
    //필터 등록이 가능하지만 필터 순서 조절이 안된다. 따라서 FilterRegistrationBean 을 사용

    //실무에서 HTTP 요청시 같은 요청의 로그에 모두 같은 식별자를 자동으로 남기는 방법은 logback mdc로

    //인터셉터와 필터가 중복되지 않도록 필터를 등록하기 위한 logFilter() 의 @Bean 은 주석처리
//    @Bean
    public FilterRegistrationBean logFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        //등록할 필터 지정, 만든 필터인 LogFilter 넣어주기
        filterRegistrationBean.setFilter(new LogFilter());
        //필터체인으로 여러 개 들어갈 수 있으므로 순서 정해주기
        filterRegistrationBean.setOrder(1);
        //필터를 적용할 URL 패턴을 지정, 모든 url에 다 적용되게,
        //어떤 페이지를 들어가도 LogFilter를 거치므로 LogFilter클래스의 로그들이 남음
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }

//    @Bean
    public FilterRegistrationBean loginCheckFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        //등록할 필터 지정, 만든 필터인  LoginCheckFilter 넣어주기
        filterRegistrationBean.setFilter(new LoginCheckFilter());
        //필터체인으로 여러 개 들어갈 수 있으므로 순서 정해주기, 순서 2번
        filterRegistrationBean.setOrder(2);
        //필터를 적용할 URL 패턴을 지정, 모든 url에 다 적용되게,
        //어떤 페이지를 들어가도 LogFilter를 거치므로 LogFilter클래스의 로그들이 남음
        //whitelist빼고 미래에 만들어지는 url 모두 설정을 안바꾸는
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }
}
