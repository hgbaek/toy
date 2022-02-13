package data;

import org.apache.xml.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public class SgEncryption
{
//    private static final Logger LOGGER = LogManager.getLogger(SgEncryption.class);

    /**
     * SHA 256 알고리즘으로 in coding 된 HashCode 를 반환합니다.
     *
     * @param text
     * @return
     */
    public static String getSHACode(String text)
    {
        String sha = null;
        try
        {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(text.getBytes());
            byte[] byteData = sh.digest();
            StringBuilder sb = new StringBuilder();
            for(byte byteDatum : byteData)
            {
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
            sha = sb.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return sha;
    }

    /**
     * byte array를 seed값으로 사용하여 AES128 키 생성
     *
     * @date 2020-05-19
     * @author hjcho
     */
    public static byte[] generateAES128Key(byte[] keys)
    {
        try
        {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keys);
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, sr);
            return kgen.generateKey().getEncoded();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static InputStream getAES128DecodeInputStream(String key, InputStream in)
    {
        InputStream input = null;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
        {

            String text = getAES128Decode(key, br.lines().collect(Collectors.joining(System.lineSeparator())));
            input = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                in.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return input;
    }

    public static String getAES128Decode(String key, String text)
    {
        String textDecode = null;
        try
        {
            byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            textDecode = new String(cipher.doFinal(Base64.decode(text)), StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return textDecode;
    }

    public static String getAES128Encode(String key, String text)
    {
        String textEncode = null;
        try
        {
            byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            textEncode = Base64.encode(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return textEncode;
    }

    public static File getAES128DecodeFile(String key, File file)
    {
        String result;
        //NIO API 사용
        //2021-02-04 hjcho
        Path filePath = file.toPath();
        try(BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            result = getAES128Decode(key, sb.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        //NIO API 사용.
        //Files.newOutputStream은 옵션을 주지 않았을 때, 파일이 있으면 덮어쓰기, 없으면 새로 생성함.
        //그러므로 createNewFile() 제거 -> SonarLint Minor 이슈 해소
        //2021-02-04 hjcho
        try(BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))
        {
            bw.write(result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public static InputStream getAES128DecodeInputStream(byte[] key, File file)
    {
        Path xmlPath = file.toPath();
        Map.Entry<byte[], byte[]> byteArrays = getByteArraysFromFile(xmlPath);

        //GCM의 경우, iv를 파일 초기 12바이트(96비트)에 저장.
        byte[] ivByteFromFile = byteArrays.getKey();
        //IV가 별도로 제공되지 않으면 파일에 포함된 값으로 계산.
        if(ivByteFromFile == null)
        {
//            LOGGER.error("[AESEncryptionType: GCM] File is corrupted. File: " + xmlPath.getFileName());
            return null;
        }
        AlgorithmParameterSpec iv = new GCMParameterSpec(96, ivByteFromFile);

        return new InflaterInputStream(new ByteArrayInputStream(getAES128Decode(key, byteArrays.getValue(), iv)));
    }

    public static byte[] getAES128Decode(byte[] key, byte[] text, AlgorithmParameterSpec iv)
    {
        byte[] byteDecode = null;
        try
        {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            byteDecode = cipher.doFinal(text);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return byteDecode;
    }

    public static File getAES128EncodeFile(String key, File file)
    {
        String result;

        //NIO API 사용
        //2021-02-04 hjcho

        Path filePath = file.toPath();
        try(BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            result = getAES128Encode(key, sb.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        //NIO API 사용.
        //Files.newOutputStream은 옵션을 주지 않았을 때, 파일이 있으면 덮어쓰기, 없으면 새로 생성함.
        //그러므로 createNewFile() 제거 -> SonarLint Minor 이슈 해소
        //2021-02-04 hjcho

        try(BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))
        {
            bw.write(result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public static byte[] getAES128Encode(byte[] key, byte[] byteText, AlgorithmParameterSpec iv)
    {
        byte[] byteEncode = null;
        try
        {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byteEncode = cipher.doFinal(byteText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return byteEncode;
    }

    private static Map.Entry<byte[], byte[]> getByteArraysFromFile(Path xmlPath)
    {
        try(InputStream fis = Files.newInputStream(xmlPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int)Files.size(xmlPath) - 12))
        {
            byte[] ivByte = new byte[12];
            byte[] buffer = new byte[4096];
            int read;

            read = fis.read(ivByte);

            while(read != -1)
            {
                read = fis.read(buffer);
                if (read > 0)
                {
                    baos.write(buffer, 0, read);
                }
            }

            return new AbstractMap.SimpleEntry<>(ivByte, baos.toByteArray());
        }
        catch(Exception e)
        {
//            LOGGER.error("[AESEncrytpion: GCM] Read byte from file failed");
        }

        return new AbstractMap.SimpleEntry<>(null, null);
    }
}
