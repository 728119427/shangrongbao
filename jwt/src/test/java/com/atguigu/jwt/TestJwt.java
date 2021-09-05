package com.atguigu.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

public class TestJwt {
    //过期时间
    private static long tokenExpiration= 24*60*60*1000;
    //密钥
    private  static String tokenSignKey="atguigu123";

    /**
     * 测试生成token
     */
    @Test
    public void test1(){
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT") //令牌类型
                .setHeaderParam("alg", "HS256")//签名算法

                .setSubject("guli-user")
                .setIssuer("atguigu1") //签发者
                .setAudience("atguigu2")//接收者
                .setIssuedAt(new Date())//签发时间
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))//过期时间
                .setNotBefore(new Date(System.currentTimeMillis() + 20000))//20秒后可用
                .setId(UUID.randomUUID().toString())

                .claim("nickname", "tom")
                .claim("age", 19)

                .signWith(SignatureAlgorithm.HS256, tokenSignKey)//签名哈希
                .compact();

        System.out.println(token);

    }


    /**
     * 测试解析token
     */
    @Test
    public void test2(){
            String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                    "eyJzdWIiOiJndWxpLXVzZXIiLCJpc3MiOiJhdGd1aWd1MS" +
                    "IsImF1ZCI6ImF0Z3VpZ3UyIiwiaWF0IjoxNjI1NjU4MTIwLCJleHAiOj" +
                    "E2MjU3NDQ1MjAsIm5iZiI6MTYyNTY1ODE0MCwianRpIjoiZTQzZTk1YmQtYzYwZS00YmZkL" +
                    "WJhNDQtZTI2NjkyYWJkMjgxIiwibmlja25hbWUiOiJ0b20iLCJhZ2UiOjE5fQ.ikLdcRc2pO7" +
                    "FKAdoch1osTF2loaAPARMl8bEfxoNs2w";
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims body = claimsJws.getBody();
        System.out.println(body.getIssuer());
        System.out.println(body.getAudience());
        System.out.println(body.getSubject());

    }
}
