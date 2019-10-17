/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.jersey;

import org.glassfish.jersey.SslConfigurator;
import org.kantega.respiro.api.RestClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static java.lang.Thread.currentThread;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;
import static org.kantega.respiro.api.RestClientBuilder.Build;

public class DefaultClientBuilder implements RestClientBuilder {
    private final Collection<ClientCustomizer> clientCustomizers;




    public DefaultClientBuilder(Collection<ClientCustomizer> clientCustomizers) {

        this.clientCustomizers = clientCustomizers;
    }

    @Override
    public Build client() {
        return new Build();
    }

    class Build implements RestClientBuilder.Build {

        private Feature basicAuth;
        private SSLContext sslContext;

        @Override
        public Build basicAuth(String username, String password) {
            this.basicAuth = basic(username, password);
            return this;
        }

        // https://stackoverflow.com/questions/25003111/how-to-authenticate-using-the-jersey-client-2-x-with-authentication-method-for

        @Override
        public Build sslAuth(String keystorePath, String keystorePassword ) {
            // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#SSLContext
            try {
                KeyManager mykm[] = new KeyManager[]{new MyX509KeyManager(keystorePath, keystorePassword.toCharArray())};

                TrustManager mytm[] = new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }}};

                SSLContext sslCtx = SSLContext.getInstance("TLS");
                sslCtx.init(mykm, mytm, new java.security.SecureRandom());
                this.sslContext = sslCtx;
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public Build sslAuth22(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) {
            // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#SSLContext

            SslConfigurator sslConfig = SslConfigurator.newInstance().securityProtocol("TLS");

            if (keystorePath != null) {
                sslConfig = sslConfig.keyStoreFile(keystorePath)
                    .keyStorePassword(keystorePassword)
                    .keyStoreType("PKCS12");
            }
            if (truststorePath != null) {
                sslConfig = sslConfig.trustStoreFile(truststorePath)
                    .trustStorePassword(truststorePassword);
            }

            this.sslContext = sslConfig.createSSLContext();
            return this;
        }


        @Override
        public Client build() {
            ClientConfig cc = new ClientConfig();

            if (basicAuth != null) {
                cc.register(basicAuth);
            }

            for (ClientCustomizer clientCustomizer : clientCustomizers) {
                clientCustomizer.customize(cc);
            }
            ClassLoader contextClassloader = currentThread().getContextClassLoader();
            try {
                currentThread().setContextClassLoader(getClass().getClassLoader());
                ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(cc);

                if (sslContext != null) {
                    clientBuilder = clientBuilder.sslContext(sslContext).hostnameVerifier(getHostnameVerifier());
                }
                return clientBuilder.build();
            } finally {
                currentThread().setContextClassLoader(contextClassloader);
            }
        }

        private HostnameVerifier getHostnameVerifier() {
            return new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                    return true;
                }
            };
        }
    }

    static class MyX509KeyManager implements X509KeyManager {

        X509KeyManager pkixKeyManager;

        MyX509KeyManager(String keyStore, char[] password)  {
            this(new File(keyStore), password);
        }

        MyX509KeyManager(File keyStore, char[] password) {
            // create a "default" JSSE X509KeyManager.
            try{
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(keyStore), password);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
                kmf.init(ks, password);

                KeyManager kms[] = kmf.getKeyManagers();

                /*
                 * Iterate over the returned keymanagers, look
                 * for an instance of X509KeyManager.  If found,
                 * use that as our "default" key manager.
                 */
                for (int i = 0; i < kms.length; i++) {
                    if (kms[i] instanceof X509KeyManager) {
                        pkixKeyManager = (X509KeyManager) kms[i];
                        return;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Couldn't initialize key manager", e);
            }
        }

        public PrivateKey getPrivateKey(String arg0) {
            return pkixKeyManager.getPrivateKey(arg0);
        }

        public X509Certificate[] getCertificateChain(String arg0) {
            return pkixKeyManager.getCertificateChain(arg0);
        }

        public String[] getClientAliases(String arg0, Principal[] arg1) {
            return pkixKeyManager.getClientAliases(arg0, arg1);
        }

        public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
            return pkixKeyManager.chooseClientAlias(arg0, arg1, arg2);
        }

        public String[] getServerAliases(String arg0, Principal[] arg1) {
            return pkixKeyManager.getServerAliases(arg0, arg1);
        }

        public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
            return pkixKeyManager.chooseServerAlias(arg0, arg1, arg2);
        }

    }
}
