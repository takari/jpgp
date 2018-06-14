package io.takari.jpgp;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

public class PGPRoundtripTest {

  String basedir;
  File sourceDirectory;
  File workingDirectory;
  
  @Test
  public void validateRoundtrip() throws Exception {
    basedir = System.getProperty("basedir", new File("").getAbsolutePath());
    sourceDirectory = new File(basedir, "src/test/files");
    workingDirectory = new File(basedir, "target/files");
    FileUtils.copyDirectory(sourceDirectory, workingDirectory);
        
    String userIdForPrivateKey = "userid";
    String passwordOfPrivateKey = "password";
    String privateKeyName = "private.gpg";
    String publicKeyName = "public.gpg";
    String messageName = "artifact.txt";
    String messageNameSignature = messageName + ".asc";
    
    // Generate a key pair for signing
    PGPKeyPairGenerator generator = new PGPKeyPairGenerator();
    try (
      OutputStream publicKeyStream = outputStream(publicKeyName);
      OutputStream privateKeyStream = outputStream(privateKeyName)) {
      generator.generateKeyPair(userIdForPrivateKey, passwordOfPrivateKey, 512, publicKeyStream, privateKeyStream);
    }

    // Sign the artifact
    PGPMessageSigner signer = new PGPMessageSigner();
    try (
      InputStream privateKeyInputStream = inputStream(privateKeyName);
      InputStream message = inputStream(messageName);
      OutputStream signatureOutputStream = outputStream(messageNameSignature)) {
      assertTrue(signer.signMessage(privateKeyInputStream, userIdForPrivateKey, passwordOfPrivateKey, message, signatureOutputStream));
    }

    // Verify the signature of the artifact
    PGPMessageSigner verifier = new PGPMessageSigner();
    try (
      InputStream publicKeyInputStream = inputStream(publicKeyName);
      InputStream message = inputStream(messageName);
      InputStream signatureInputStream = inputStream(messageNameSignature)) {
      assertTrue(verifier.verifyMessage(publicKeyInputStream, message, signatureInputStream));
    }
  }

  private InputStream inputStream(String name) throws IOException {
    return new FileInputStream(file(name));
  }

  private OutputStream outputStream(String name) throws IOException {
    return new FileOutputStream(file(name));
  }

  private File file(String name) {
    return new File(workingDirectory, name);
  }
}
