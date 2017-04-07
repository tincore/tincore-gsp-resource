package com.tincore.gsp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.JamonPerformanceMonitorInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.github.ziplet.filter.compression.CompressingFilter;

@SpringBootApplication
@EnableResourceServer
@RestController
@EnableCaching
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class GspResourceApplication extends ResourceServerConfigurerAdapter {
	public final static String API_PREFIX = "/api/v1";

	@Autowired
	private H2ConsoleProperties h2ConsoleProperties;

	@Value("${gsp.security.disabled:false}")
	private boolean oauth2Disabled;

	@Override
	public void configure(HttpSecurity httpSecurity) throws Exception {
		if (oauth2Disabled) {
			httpSecurity.addFilterBefore(new Filter() {
				Authentication singleUserAuthentication;
				{
					singleUserAuthentication = new Authentication() {

						private static final long serialVersionUID = 1L;
						List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
						{
							grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
						}

						@Override
						public String getName() {
							return "singleuser";
						}

						@Override
						public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
						}

						@Override
						public boolean isAuthenticated() {
							return true;
						}

						@Override
						public Object getPrincipal() {
							return getName() + "_principal";
						}

						@Override
						public Object getDetails() {
							return getName() + "_details";
						}

						@Override
						public Object getCredentials() {
							return getName() + "_credentials";
						}

						@Override
						public Collection<? extends GrantedAuthority> getAuthorities() {
							return grantedAuthorities;
						}
					};
				}

				@Override
				public void init(FilterConfig filterConfig) throws ServletException {

				}

				@Override
				public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
						throws IOException, ServletException {
					Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
					if (authentication == null) {
						SecurityContextHolder.getContext().setAuthentication(singleUserAuthentication);
						chain.doFilter(request, response);
					}
				}

				@Override
				public void destroy() {
				}
			}, UsernamePasswordAuthenticationFilter.class).authorizeRequests().antMatchers("/").authenticated();
		} else {
			httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//
					.and().authorizeRequests()//
					.antMatchers("/api/**").access("#oauth2.hasScope('openid')")//
					.antMatchers(HttpMethod.GET, "/api/**").access("#oauth2.hasScope('read')")//
					.antMatchers(HttpMethod.POST, "/api/**").access("#oauth2.hasScope('write')");//
			// .antMatchers(HttpMethod.GET,
			// "api/**").access("#oauth2.hasScope('read')")//
			// .antMatchers(HttpMethod.POST,
			// "api/**").access("#oauth2.hasScope('write')");
			if (h2ConsoleProperties.getEnabled()) {
				// Need this for h2 console
				httpSecurity.csrf().disable();
				httpSecurity.headers().frameOptions().disable();
			} else {
				httpSecurity.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
			}

		}
	}

	public static void main(String[] args) {
		SpringApplication.run(GspResourceApplication.class, args);
	}

	@Bean
	@Profile("!cloud")
	public Filter getCommonsRequestLoggingFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(5120);
		return filter;
	}

	@Bean
	public CompressingFilter getCompressingFilter() {
		return new com.github.ziplet.filter.compression.CompressingFilter();
	}

	@Bean
	@Profile("performance")
	public Filter getJamonFilter() {
		return new com.jamonapi.JAMonFilter();
	}

	@Bean
	@Profile("performance")
	public JamonPerformanceMonitorInterceptor getJamonPerformanceMonitorInterceptor() {
		return new JamonPerformanceMonitorInterceptor();
	}

	@Bean
	@Profile("performance")
	public Advisor getJamonPerformanceAdvisor() {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression("execution(public * com.tincore..*.*(..))");
		return new DefaultPointcutAdvisor(pointcut, getJamonPerformanceMonitorInterceptor());
	}

}
