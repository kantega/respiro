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

package org.kantega.respiro.documenter;

import fj.data.List;
import fj.test.Gen;
import fj.test.Property;
import fj.test.reflect.Name;
import fj.test.runner.PropertyTestRunner;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;

@RunWith(PropertyTestRunner.class)
public class NormalizeUrlTest {

    final static String words =
      "Bacon ipsum dolor amet pork loin cupim shankle tail, " +
      "ham strip steak ground round shank ribeye pig spare ribs " +
        "sirloin doner turducken. Meatloaf ham hock sausage cow, " +
        "bresaola tail tongue beef sirloin prosciutto shank pig rump doner. " +
        "Drumstick turkey landjaeger, boudin swine pork belly andouille kielbasa " +
        "meatball brisket. Sausage andouille meatloaf pork chop tail short loin " +
        "pork pancetta rump leberkas.";

    final static List<String> listOfWords =
      List.arrayList(StringUtils.split(StringUtils.remove(words,".")," "));

    final static Gen<String> randWord =
      Gen.pickOne(listOfWords);

    final static Gen<String> randSlash =
      Gen.elements("", "/");

    final static Gen<String> randBeginOrEnd =
      randSlash.bind(pref->randWord.bind(word->randSlash.map(postfix -> pref+word+postfix)));



    @Name("A normalized string always start with s slash and never ends with one")
    public static final Property p1 =
      Property.property(randBeginOrEnd,str->{
          String normalized = Strings.normalizeUrl(str);
          return Property.prop(StringUtils.startsWith(normalized,"/") && !StringUtils.endsWith(normalized,"/"));

      });


}
