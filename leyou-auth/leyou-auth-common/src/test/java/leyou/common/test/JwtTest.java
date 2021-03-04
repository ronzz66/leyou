package leyou.common.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "C:\\tmp\\rsa\\rsa.pub";//公钥路径

    private static final String priKeyPath = "C:\\tmp\\rsa\\rsa.pri";//私钥路径

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");//生成公钥和私钥
    }

    @Before//读取公钥私钥
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token  载荷，私钥， 过期时间
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {//解析token
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTYwNjIwOTcyN30.eejtShoy3YExEp16Rao2EVwDLr4hwvA-eWRcXDGERnWJSuPx4MGQCrmYj4tgig8HsxBbg-d3BHuejeDKEuEoeA9FMqaV30ibWXwAKt1o8MApUbZ3Y1lxm4yukKJ2-oQpM6OIXcMP4DHofLRakHLD5NHMWLk5ErGpuVovmafP76U";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}