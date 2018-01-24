/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.test.sshd;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryDecoder;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.UnknownCommand;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.kantega.respiro.api.Initializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

/**
 *
 */

@Plugin
public class TestSshdPlugin implements Initializer{

    private final Optional<SshServer> sshd;


    @Export
    final Initializer initializer = this;

    public TestSshdPlugin() throws IOException {

        File baseDir = new File(System.getProperty("reststopPluginDir"));

        File sourceFiles = new File(baseDir, "src/test/sshd");

        if (sourceFiles.exists()) {


            File homeDir = new File(baseDir, "target/sshd");

            this.sshd = Optional.of(SshServer.setUpDefaultServer());

            SshServer sshd = this.sshd.get();

            sshd.setPort(0);


            SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder().build();
            sshd.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));


            File contentDir = new File(homeDir, "content");

            contentDir.mkdirs();


            copySshdFilesToContentDirectory(sourceFiles.toPath(), contentDir.toPath());


            sshd.setCommandFactory(command -> {
                if ("resetfiles".equals(command)) {
                    return new ResetDataCommand(sourceFiles.toPath(), contentDir.toPath());
                }
                return new UnknownCommand(command);
            });

            sshd.setFileSystemFactory(new VirtualFileSystemFactory(contentDir.toPath()));

            SimpleGeneratorHostKeyProvider keyProvider = new SimpleGeneratorHostKeyProvider(new File(homeDir, "hostkey.ser"));
            sshd.setKeyPairProvider(keyProvider);

            sshd.setPasswordAuthenticator((username, password, session) -> username.equals(password));

            sshd.start();

            writeFingerprintAndKnownHosts(keyProvider, sshd.getPort());

            writePort(sshd.getPort());


        } else {
            sshd = Optional.empty();
        }
    }

    private void copySshdFilesToContentDirectory(Path sshdFiles, Path dest) throws IOException {

        Files.walkFileTree(dest, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                file.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                dir.toFile().delete();
                return FileVisitResult.CONTINUE;
            }
        });

        dest.toFile().mkdirs();

        Files.walkFileTree(sshdFiles, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path relativize = sshdFiles.relativize(file);

                Path dst = dest.resolve(relativize);

                dst.toFile().getParentFile().mkdirs();
                Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path relativize = sshdFiles.relativize(dir);

                Path dst = dest.resolve(relativize);

                dst.toFile().mkdirs();

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void writePort(int port) throws IOException {

        Files.write(new File(System.getProperty("reststopPluginDir"), "target/test-classes/sshdPort.txt").toPath(),
          Integer.toString(port).getBytes());
        System.setProperty("sshdPort", Integer.toString(port));
    }

    private void writeFingerprintAndKnownHosts(
      SimpleGeneratorHostKeyProvider keyProvider,
      int port) throws IOException {
        for (KeyPair keyPair : keyProvider.loadKeys()) {
            String fingerPrint = KeyUtils.getFingerPrint(keyPair.getPublic());
            System.setProperty("sshdFingerprint", fingerPrint);
            Files.write(new File(System.getProperty("reststopPluginDir"), "target/test-classes/sshdFingerprint.txt").toPath(),
              fingerPrint.getBytes());


            PublicKeyEntryDecoder<PublicKey, PrivateKey> publicKeyEntryDecoder = (PublicKeyEntryDecoder<PublicKey, PrivateKey>) KeyUtils.getPublicKeyEntryDecoder(keyPair.getPublic());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();


            publicKeyEntryDecoder.encodePublicKey(bytes, keyPair.getPublic());


            ByteArrayOutputStream out = new ByteArrayOutputStream();

            out.write(("[localhost]:" + port + " ssh-dss ").getBytes());
            out.write(Base64.getEncoder().encode(bytes.toByteArray()));
            out.write("\n".getBytes());


            Path knownHostsFile = new File(System.getProperty("reststopPluginDir"), "target/test-classes/known_hosts").toPath();
            System.setProperty("sshdKnownHostsFile", knownHostsFile.toFile().getAbsolutePath());
            Files.write(knownHostsFile, out.toByteArray());

        }
    }

    @PreDestroy
    public void stop() throws IOException {
        if (sshd.isPresent()) {
            sshd.get().stop();
        }
    }

    @Override
    public void initialize() {
        
    }


    private class ResetDataCommand implements Command {
        private final Path source;
        private final Path dest;
        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;

        public ResetDataCommand(Path source, Path dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void setInputStream(InputStream in) {

            this.in = in;
        }

        @Override
        public void setOutputStream(OutputStream out) {

            this.out = out;
        }

        @Override
        public void setErrorStream(OutputStream err) {

            this.err = err;
        }

        @Override
        public void setExitCallback(ExitCallback callback) {

            this.callback = callback;
        }

        @Override
        public void start(Environment env) throws IOException {
            new Thread() {
                @Override
                public void run() {
                    try {
                        copySshdFilesToContentDirectory(source, dest);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    finally {
                        callback.onExit(0);
                    }
                }
            }.start();


        }
    }
}

