/*
 * Copyright 2012-2016 MongDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package course;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class BlogPostDAO {
    private final MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    public Document findByPermalink(String permalink) {

        Bson query = Filters.eq("permalink", permalink);
        Document post = postsCollection.find(query).first();

        return post;
    }

    public List<Document> findByDateDescending(int limit) {

        Bson sort = Sorts.descending("date");

        List<Document> posts = postsCollection.find()
            .sort(sort)
            .limit(limit)
            .into(new LinkedList<>());

        return posts;
    }

    public List<Document> findByTagDateDescending(final String tag) {
        return postsCollection.find(Filters.eq("tags", tag))
                       .sort(Sorts.descending("date"))
                       .limit(10)
                       .into(new ArrayList<Document>());
    }

    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        String permLinkExtra = String.valueOf(GregorianCalendar
                .getInstance().getTimeInMillis());
        permalink += permLinkExtra;

        Document post = new Document("permalink", permalink)
            .append("author", username)
            .append("title", title)
            .append("body", body)
            .append("tags", tags)
            .append("comments", Collections.emptyList())
            .append("date", new java.util.Date());

        try {

            postsCollection.insertOne(post);
            System.out.println("Inserting blog post with permalink " + permalink);
        } catch (Exception e) {
            System.out.println("Error inserting post");
            return null;
        }

        return permalink;
    }

    public void addPostComment(final String name, final String email, final String body, final String permalink) {
        Document comment = new Document("author", name)
            .append("body", body);

        if (email != null && !email.trim().isEmpty()) {
            comment = comment.append("email", email);
        }

        Bson query = Filters.eq("permalink", permalink);
        Bson update = Updates.push("comments", comment);
        postsCollection.updateOne(query, update);
    }

}
