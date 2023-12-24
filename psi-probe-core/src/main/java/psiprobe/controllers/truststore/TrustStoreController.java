/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.truststore;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
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

@Controller
public class TrustStoreController extends AbstractTomcatContainerController {

  private static final Logger mylogger = LoggerFactory.getLogger(TrustStoreController.class);

  @GetMapping(path = "/truststore.htm")
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

      // Validate and sanitize user input (trustStore)
      String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
      if (StringUtils.isNotBlank(trustStorePath)) {
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
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
      String errorMessage = "Failed to load keystore: " + e.getMessage();
      mylogger.error(errorMessage, e);
      throw new KeyStoreLoadException(errorMessage, e);
    }
  }

  // Validate and sanitize user input (trustStorePassword)
  private char[] getPasswordFromUserInput() {
    String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
    return StringUtils.isNotBlank(trustStorePassword) ? trustStorePassword.toCharArray() : new char[0];
  }

  @Value("truststore")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }
}
