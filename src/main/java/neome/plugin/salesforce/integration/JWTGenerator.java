package neome.plugin.salesforce.integration;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import com.neome.api.meta.base.Types;
import com.neome.srvc.Srvc;
import com.neome.util.cfg.Cfg;
import com.neome.util.cfg.CfgAppServer;
import com.neome.util.net.base.MediaExchangeStatus;
import com.neome.util.net.client.media.common.MediaClientConfig;
import com.neome.util.net.client.media.download.MediaFileDownload;
import com.neome.util.net.client.media.download.MediaFileDownloadJob;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class JWTGenerator
{
  public static JWTGenerator INSTANCE = new JWTGenerator();

  private JWTGenerator()
  {

  }

  public String generate(Types.EntId entId,
    Types.MediaIdDocument privateKeyMediaIdDocument,
    String clientId, String username)
  {
    RSAPrivateKey privateKey = getPrivateKey(privateKeyMediaIdDocument, entId);
    JWSSigner signer = new RSASSASigner(privateKey);
    Date now = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.HOUR, 1);
    //Date tokenExpiration = calendar.getTime();
    Date expirationTime = new Date(now.getTime() + 60 * 1000);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
      .issuer(clientId)
      .subject(username)
      .audience("https://login.salesforce.com")
      .issueTime(now)
      .expirationTime(expirationTime)
      .claim("client_id", clientId)
      .build();
    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
    try
    {
      signedJWT.sign(signer);
    }
    catch(JOSEException e)
    {
      throw new RuntimeException(e);
    }
    return signedJWT.serialize();
  }

  private synchronized RSAPrivateKey getPrivateKey(Types.MediaId mediaId, Types.EntId entId)
  {
    File dir = new File(Srvc.local.privateDir, entId.getId());
    String bearerToken = Srvc.app.auth.getBearerToken();
    File privateKeyFile = new File(dir.getAbsolutePath() + "/" + mediaId.getId());
    if(privateKeyFile.exists())
    {
      return loadPrivateKey(privateKeyFile.getAbsolutePath());
    }
    MediaClientConfig config = new MediaClientConfig(bearerToken);
    MediaFileDownloadJob mediaFileDownloadJob = new MediaFileDownloadJob(config, entId, mediaId, privateKeyFile);
    MediaFileDownload mediaFileDownload = new MediaFileDownload(mediaFileDownloadJob, (mediaJobKey, progress) -> {

    });
    CfgAppServer appServerCfg = Cfg.cfg.getAppServer();
    MediaExchangeStatus mediaExchangeStatus = mediaFileDownload.execute(appServerCfg.getHost(),
      appServerCfg.getPort(),
      appServerCfg.getSsl());
    if(mediaExchangeStatus == MediaExchangeStatus.success)
    {
      return loadPrivateKey(privateKeyFile.getAbsolutePath());
    }
    return null;
  }

  private RSAPrivateKey loadPrivateKey(String privateKeyPath)
  {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    try(Reader reader = new FileReader(privateKeyPath);
      PemReader pemReader = new PemReader(reader))
    {
      PemObject pemObject = pemReader.readPemObject();
      byte[] privateKeyBytes = pemObject.getContent();
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = keyFactory.generatePrivate(spec);
      return (RSAPrivateKey) privateKey;
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
