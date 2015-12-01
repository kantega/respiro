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
import java.nio.file.Files;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by helaar on 26.11.2015.
 */
public class FileContentMatcher extends TypeSafeMatcher<File> {

    private final File fileMatched;
    private final String search;
    private final String replace;
    private int lineNo;
    private String mLine;
    private String iLine;


    public FileContentMatcher(File fileMatched, String search, String replace) {
        this.fileMatched = fileMatched;
        this.search = search;
        this.replace = replace;
    }

    @Override
    protected boolean matchesSafely(File item) {

        try (Stream<String> mStream = Files.lines(fileMatched.toPath());
             Stream<String> iStream = Files.lines(item.toPath())){
            lineNo =0;
            Iterator<String> mIt = mStream.iterator();
            Iterator<String> iIt = iStream.iterator();

            while(mIt.hasNext() && iIt.hasNext()) {

                lineNo++;
                mLine = mIt.next();
                iLine = iIt.next();

                String modLine = search != null && replace != null ? mLine.replaceAll(search,replace)
                        : mLine;

                if((iLine).compareTo((modLine)) != 0)
                    return false;
            }
            if(fileMatched.length() != item.length()) {
                mLine = "Filesize: " + String.valueOf(fileMatched.length());
                iLine = "Filesize: " + String.valueOf(item.length());
                return false;
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void describeTo(Description description) {

        if(mLine != null) {
            description.appendText(mLine).appendValue(iLine);
        }

    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static FileContentMatcher hasSameContentAs(File item) {
        return new FileContentMatcher(item, null, null);
    }
    public static FileContentMatcher hasSameContentAs(File item, String search, String replace) {
        return new FileContentMatcher(item, search, replace);
    }
}
