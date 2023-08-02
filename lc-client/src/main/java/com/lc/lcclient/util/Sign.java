package com.lc.lcclient.util;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @Author Lc
 * @Date 2023/7/27
 * @PackageName: com.lc.interfaceinfo.util
 * @ClassName: Sign
 * @Description: 加密解密
 */

public class Sign {

    public static String getSign(String body,String secretKey){
        Digester SHA256 = new Digester(DigestAlgorithm.SHA256);
// 5393554e94bf0eb6436f240a4fd71282
        String digestHex = SHA256.digestHex(body+secretKey);
        return digestHex;
    }
}
