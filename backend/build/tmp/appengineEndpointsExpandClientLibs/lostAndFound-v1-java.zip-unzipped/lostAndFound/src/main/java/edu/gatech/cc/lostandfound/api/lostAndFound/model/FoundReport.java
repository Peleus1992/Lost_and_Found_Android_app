/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2015-11-16 19:10:01 UTC)
 * on 2016-01-01 at 17:12:42 UTC 
 * Modify at your own risk.
 */

package edu.gatech.cc.lostandfound.api.lostAndFound.model;

/**
 * Model definition for FoundReport.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the lostAndFound. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class FoundReport extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime created;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String description;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String image;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private GeoPt location;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean returned;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime timeFound;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String title;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userId;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userNickname;

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getCreated() {
    return created;
  }

  /**
   * @param created created or {@code null} for none
   */
  public FoundReport setCreated(com.google.api.client.util.DateTime created) {
    this.created = created;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDescription() {
    return description;
  }

  /**
   * @param description description or {@code null} for none
   */
  public FoundReport setDescription(java.lang.String description) {
    this.description = description;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public FoundReport setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @see #decodeImage()
   * @return value or {@code null} for none
   */
  public java.lang.String getImage() {
    return image;
  }

  /**

   * @see #getImage()
   * @return Base64 decoded value or {@code null} for none
   *
   * @since 1.14
   */
  public byte[] decodeImage() {
    return com.google.api.client.util.Base64.decodeBase64(image);
  }

  /**
   * @see #encodeImage()
   * @param image image or {@code null} for none
   */
  public FoundReport setImage(java.lang.String image) {
    this.image = image;
    return this;
  }

  /**

   * @see #setImage()
   *
   * <p>
   * The value is encoded Base64 or {@code null} for none.
   * </p>
   *
   * @since 1.14
   */
  public FoundReport encodeImage(byte[] image) {
    this.image = com.google.api.client.util.Base64.encodeBase64URLSafeString(image);
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public GeoPt getLocation() {
    return location;
  }

  /**
   * @param location location or {@code null} for none
   */
  public FoundReport setLocation(GeoPt location) {
    this.location = location;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getReturned() {
    return returned;
  }

  /**
   * @param returned returned or {@code null} for none
   */
  public FoundReport setReturned(java.lang.Boolean returned) {
    this.returned = returned;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getTimeFound() {
    return timeFound;
  }

  /**
   * @param timeFound timeFound or {@code null} for none
   */
  public FoundReport setTimeFound(com.google.api.client.util.DateTime timeFound) {
    this.timeFound = timeFound;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTitle() {
    return title;
  }

  /**
   * @param title title or {@code null} for none
   */
  public FoundReport setTitle(java.lang.String title) {
    this.title = title;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserId() {
    return userId;
  }

  /**
   * @param userId userId or {@code null} for none
   */
  public FoundReport setUserId(java.lang.String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserNickname() {
    return userNickname;
  }

  /**
   * @param userNickname userNickname or {@code null} for none
   */
  public FoundReport setUserNickname(java.lang.String userNickname) {
    this.userNickname = userNickname;
    return this;
  }

  @Override
  public FoundReport set(String fieldName, Object value) {
    return (FoundReport) super.set(fieldName, value);
  }

  @Override
  public FoundReport clone() {
    return (FoundReport) super.clone();
  }

}