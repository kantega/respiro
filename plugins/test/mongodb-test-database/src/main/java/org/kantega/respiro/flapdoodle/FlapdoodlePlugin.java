package org.kantega.respiro.flapdoodle;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import org.kantega.respiro.api.Initializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;

@Plugin
public class FlapdoodlePlugin implements Initializer {

    private final String basedir = getProperty("reststopPluginDir");

    @Export
    private final Initializer initializer = this;

    private MongodProcess process;

    @Override
    public void initialize() {

        if( getProperty("flapdoodlePort") == null) {
            try {

                System.out.println("Starting Flapdoodle Plugin");

                final Path tempdir = Paths.get(basedir, "target", "flapdoodle");
                tempdir.toFile().mkdirs();

                setProperty("de.flapdoodle.embed.io.tmpdir", tempdir.toString());

                final Command command = Command.MongoD;
                final PropertyOrPlatformTempDir tempDirFactory = new PropertyOrPlatformTempDir();
                final IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(command)
                    .artifactStore(new ExtractedArtifactStoreBuilder()
                        .defaults(command)
                        .tempDir(tempDirFactory)
                        .download(new DownloadConfigBuilder()
                            .defaultsForCommand(command).build())
                        .executableNaming(new UserTempNaming() {
                            @Override
                            public String nameFor(String prefix, String postfix) {
                                String name = super.nameFor(prefix, postfix);
                                try {
                                    // extracted file will not be cleaned up properly when process is killed,
                                    // causing an error the next time you try to run reststop:run.
                                    // this notably happens when starting/stopping reststop:run from IntelliJ IDEA.
                                    // this code will delete any old extracted file before creating a new one.
                                    Files.deleteIfExists(new File(tempDirFactory.asFile(), name).toPath());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return name;
                            }
                        }))
                    .build();

                final MongodStarter runtime =
                    MongodStarter.getInstance(runtimeConfig);

                final IMongodConfig mongodConfig =
                    new MongodConfigBuilder()
                        .version(Version.Main.DEVELOPMENT )
                        .build();

                final MongodExecutable mongodExecutable =
                    runtime.prepare(mongodConfig);
                process = mongodExecutable.start();


                setProperty("flapdoodlePort", Integer.toString(mongodConfig.net().getPort()));
                write(new File(basedir, "target/test-classes/flapdoodlePort.txt").toPath(),
                    Integer.toString(mongodConfig.net().getPort()).getBytes());

                setupDatabases(new MongoClient(mongodConfig.net().getBindIp(), mongodConfig.net().getPort()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            System.out.println("Flapdoodle already running on port " + getProperty("flapdoodlePort"));
        }
            
    }

    private void setupDatabases(MongoClient client) throws IOException {

        final File dummyBasedir = new File(basedir, "src/test/mongo");
        final File[] dirs = dummyBasedir.listFiles(File::isDirectory);
        // create user scriptlet:
        final String adminUserString = "{ \"createUser\": \"admin\",  \"pwd\": \"password\",\"roles\": [\"readWrite\"]}";


        if (dirs != null) {
            for (File dir : dirs) {
                MongoDatabase database = client.getDatabase(dir.getName());
                database.runCommand(BasicDBObject.parse(adminUserString));
                File[] files = dir.listFiles(pathname -> pathname.getName().endsWith(".mongo"));
                for (File file : files) {
                    String fileContent = new String(readAllBytes(file.toPath()), "utf-8");

                    database.runCommand(BasicDBObject.parse(fileContent));

                }


            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (process != null && process.isProcessRunning())
            process.stop();
    }


}
