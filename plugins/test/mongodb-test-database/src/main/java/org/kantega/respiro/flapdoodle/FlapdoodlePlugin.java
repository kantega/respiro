package org.kantega.respiro.flapdoodle;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.conversions.Bson;
import org.kantega.respiro.api.DataSourceInitializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;

@Plugin
public class FlapdoodlePlugin implements DataSourceInitializer {

    private final String basedir = getProperty("reststopPluginDir");

    @Export
    private final DataSourceInitializer initializer = this;


    private MongodProcess process;

    private MongoClient client;

    @Override
    public void initialize() {

        try {

            final Command command = Command.MongoD;
            final IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                    .defaults(command)
                    .download(new DownloadConfigBuilder()
                        .defaultsForCommand(command).build())
                    .executableNaming(new UserTempNaming()))
                .build();

            final MongodStarter runtime =
                MongodStarter.getInstance(runtimeConfig);

            final IMongodConfig mongodConfig =
                new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
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
                for(File file : files){
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
