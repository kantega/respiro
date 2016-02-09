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

package org.kantega.respiro.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**

 */
public class FileMetricsChecker extends TypeSafeMatcher<File> {

    private final String namePrefix;
    private final long filesize;
    private final int linelength;
    private final int lines;
    private final String charset;

    private StringBuilder message = new StringBuilder();

    public FileMetricsChecker(String namePrefix, long filesize, int linelength, int lines) {
        this.namePrefix = namePrefix;
        this.filesize = filesize;
        this.linelength = linelength;
        this.lines = lines;
        this.charset = "utf-8";
    }

    public FileMetricsChecker(String charset, String namePrefix, long filesize, int linelength, int lines) {
        this.namePrefix = namePrefix;
        this.filesize = filesize;
        this.linelength = linelength;
        this.lines = lines;
        this.charset = charset;
    }

    @Override
    protected boolean matchesSafely(File file) {
        if (file == null)
            return false;

        if (file.getName().startsWith(namePrefix)) {
            if(file.length() != filesize)
                message.append("\nFilesize was ").append(file.length()).append(", expected ").append(filesize);

            try {
                List<String> fileLines = Files.readAllLines(file.toPath(), Charset.forName(charset));
                if( fileLines.size() != lines)
                    message.append("\nNumber of lines was ").append(fileLines.size()).append(", expected ").append(lines);

                int lineNo = 0;
                for (String oneLine : fileLines) {
                    lineNo++;

                    if( oneLine.length() != linelength ) {
                        message.append("\nLength of line #").append(lineNo).append(" was ").append(oneLine.length()).append(", expected ").append(linelength);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(message.length() > 0)
                return false;
            else
                return true;
        } else {
            message.append(namePrefix).append(" doesn't match ").append(file.getName()).append(". File is ignored.");
            return true;
        }
    }

    @Override
    public void describeTo(Description description) {

        if(message.length() > 0)
            description.appendText(message.toString());

    }

    public static FileMetricsChecker hasMetrics(String prefix, long filesize, int linelength, int lines){
        return new FileMetricsChecker(prefix, filesize, linelength, lines);
    }
    public static FileMetricsChecker hasMetrics(String charset, String prefix, long filesize, int linelength, int lines){
        return new FileMetricsChecker(charset, prefix, filesize, linelength, lines);
    }
}
