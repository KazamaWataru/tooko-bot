/*
 * Copyright (c) 2019 - 2020 KazamaWataru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tooko.core.totp;

import org.bson.codecs.pojo.annotations.*;
import tooko.core.db.*;

import java.util.*;

public class TotpData {

    public static Table<Long, TotpData> DATA = new Table<>("totp", TotpData.class);

    @BsonId
    public long chatId;

    public LinkedHashMap<String, String> totp;

}
