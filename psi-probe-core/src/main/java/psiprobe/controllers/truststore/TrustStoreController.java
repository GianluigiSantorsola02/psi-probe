/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.truststore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.controllers.certificates.KeyStoreLoadException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * The Class TrustStoreController.
 */
@Controller
public class TrustStoreController extends AbstractTomcatContainerController {


  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(TrustStoreController.class);

  @RequestMapping(path = "/truststore.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    List<Map<String, String>> certificateList = new ArrayList<>();
    try {
      String trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
      KeyStore ks;
      if (trustStoreType != null) {
        ks = KeyStore.getInstance(trustStoreType);
      } else {
        ks = KeyStore.getInstance("JKS");
      }
      String trustStore = System.getProperty("javax.net.ssl.trustStore");
      String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
      if (trustStore != null) {
        loadKeyStore(Files.newInputStream(Paths.get(trustStore)), trustStorePassword.toCharArray());

        Map<String, String> attributes;
        for (String alias : Collections.list(ks.aliases())) {
          attributes = new HashMap<>();
          if ("X.509".equals(ks.getCertificate(alias).getType())) {
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            attributes.put("alias", alias);
            attributes.put("cn", cert.getSubjectX500Principal().toString());
            attributes.put("expirationDate",
                new SimpleDateFormat("yyyy-MM-dd").format(cert.getNotAfter()));
            certificateList.add(attributes);
          }
        }
      }
    } catch (KeyStoreException e) {
      mylogger.error("There was an exception obtaining truststore: ", e);
    }
    ModelAndView mv = new ModelAndView(getViewName());
    mv.addObject("certificates", certificateList);
    return mv;
  }

  private void loadKeyStore(InputStream fis, char[] chars) throws KeyStoreLoadException {
    try {
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(fis, chars);
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
      mylogger.error("", e);
    } catch (IOException e) {
        throw new KeyStoreLoadException(e.getMessage(), e);
    }
  }

  @Value("truststore")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
