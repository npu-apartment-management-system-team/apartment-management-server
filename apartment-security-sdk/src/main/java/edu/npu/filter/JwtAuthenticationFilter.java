package edu.npu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.util.JwtTokenProvider;
import edu.npu.vo.R;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private UserDetailsService userDetailsService;

    //白名单
    private static List<String> whitelist = null;

    static {
        //加载白名单
        try (
                InputStream resourceAsStream =
                        JwtAuthenticationFilter.class
                                .getResourceAsStream("/security-whitelist.properties")
        ) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            Set<String> strings = properties.stringPropertyNames();
            whitelist = new ArrayList<>(strings);
        } catch (Exception e) {
            log.error("加载/security-whitelist.properties出错:{}", e.getMessage());
        }
    }

    @Override
    protected void doFilterInternal(
            // 加上 @NonNull 注解，可以让 IDEA 不提示参数不能为空
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        log.info("requestUrl:{}", requestUrl);
        //白名单放行
        for (String url : whitelist) {
            if (pathMatcher.match(url, requestUrl)) {
                log.info("已放行:url-{},requestUrl:{}", url, requestUrl);
                filterChain.doFilter(request, response);
                return;
            }
        }
        final String authHeader = getJwtFromRequest(request);
        final String username;
        if (StringUtils.hasText(authHeader)) {
            try {
                // 如果token过期了的话这地方会直接抛异常 轮不到我们走redis 所以要先处理
                // 否则就会走全局异常处理了
                username = jwtTokenProvider.extractUsername(authHeader);
            } catch (ExpiredJwtException e) {
                constructExpireResp(response,
                        ResponseCodeEnum.ACCESS_TOKEN_EXPIRED_ERROR,
                        "token已过期");
                return;
            }
            // 用户未登录 所携带token为空 需要验证用户名密码后签发token
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 网关走过redis了 其他服务都可以不走 验证用户名密码
                // 从redis中获取用户信息
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.isTokenValid(authHeader, userDetails)) {
                    // 将用户信息放入 SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    /*
                     * SecurityContextHolder本身是一个ThreadLocal，无法被微服务中的其他服务访问到
                     * 因此我们需要所有请求都经过这样一个过滤器，将请求头中的token解析出来
                     * 然后将用户信息放入SecurityContextHolder
                     */
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute("Authorization", "Bearer " + authHeader);
                    filterChain.doFilter(request, response);
                }
            }
        } else {
            constructExpireResp(response,
                    ResponseCodeEnum.USER_UNAUTHENTICATED,
                    "token不可为空");
        }
    }

    private void constructExpireResp(HttpServletResponse response,
                                     ResponseCodeEnum accessTokenExpiredError,
                                     String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        R.error(accessTokenExpiredError,
                                message)));
    }

    // Bearer <token>
    private String getJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return "";
    }
}
