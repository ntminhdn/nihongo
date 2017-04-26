package com.honkidenihongo.pre.dac;

import com.honkidenihongo.pre.common.config.Definition;
import com.honkidenihongo.pre.model.Ranking;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * * Reference : https://github.com/realm/realm-java/blob/master/examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java
 * <p>
 * Class DatabaseMigration dùng để support khi ta nâng version của ứng dụng upload lên store muốn import database cũ của người dùng vô database mới từ verision mới nhất của app.
 *
 * @author binh.dt.
 * @since 13-Jan-2017.
 */
public class DatabaseMigration implements RealmMigration {
    // Reference :  http://stackoverflow.com/questions/36907001/open-realm-with-new-realmconfiguration.
    private static final int HASH_CODE = 500;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        // Database phần lesson thay đổi ở version 1.
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema lessonSchema = schema.get(Definition.Database.Lesson.LESSON_NAME_TABLE);

            lessonSchema.addField(Definition.Database.Lesson.LESSON_FIELD_TITLE_VN, String.class, FieldAttribute.REQUIRED);

            lessonSchema.addField(Definition.Database.Lesson.LESSON_FIELD_TITLE_EN, String.class, FieldAttribute.REQUIRED);

            lessonSchema.addField(Definition.Database.Lesson.LESSON_FIELD_TITLE_JA, String.class, FieldAttribute.REQUIRED);
        }

        // Add field picture_file and lesson_number inside table Knowledge from database version 2 up.
        if (oldVersion < 2) {
            /**
             * Process table Knowledge.
             */
            RealmObjectSchema knowledgeSchema = schema.get(Definition.Database.Knowledge.KNOWLEDGE_NAME_TABLE);
            final RealmObjectSchema lessonSchema = schema.get(Definition.Database.Lesson.LESSON_NAME_TABLE);

            // At version 2 remove field lesson id inside table Question.
            knowledgeSchema.addField(Definition.Database.Knowledge.KNOWLEDGE_FIELD_PICTURE_FILE, String.class);

            knowledgeSchema.addField(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, Integer.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(final DynamicRealmObject objKnowledge) {
                            // Tiến hành xét giá trị cho trường lesson_number thêm vào bằng việc query con lesson có id trùng với trường lesson_id của đối tượng
                            // sau đó remove trường lesson_id của đối tượng đi.
                            final long lesson_id = objKnowledge.getLong(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_ID);

                            lessonSchema.transform(new RealmObjectSchema.Function() {
                                @Override
                                public void apply(DynamicRealmObject objLesson) {
                                    long idOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_ID);
                                    long numberOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_NUMBER);

                                    if (lesson_id == idOfLesson) {
                                        objKnowledge.setInt(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_NUMBER, (int) numberOfLesson);
                                    }
                                }
                            });
                        }
                    }).removeField(Definition.Database.Knowledge.KNOWLEDGE_FIELD_LESSON_ID);

            /**
             * Process table Question.
             */
            RealmObjectSchema questionSchema = schema.get(Definition.Database.Question.QUESTION_NAME_TABLE);

            // At version 2 remove field lesson_id inside table Question.
            questionSchema.addField(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, Integer.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(final DynamicRealmObject objQuestion) {
                            // Tiến hành xét giá trị cho trường lesson_number thêm vào bằng việc query con lesson có id trùng với trường lesson_id của đối tượng
                            // sau đó remove trường lesson_id của đối tượng đi.
                            final long lesson_id = objQuestion.getLong(Definition.Database.Question.QUESTION_FIELD_LESSON_ID);

                            lessonSchema.transform(new RealmObjectSchema.Function() {
                                @Override
                                public void apply(final DynamicRealmObject objLesson) {
                                    long idOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_ID);
                                    long numberOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_NUMBER);

                                    if (lesson_id == idOfLesson) {
                                        objQuestion.setInt(Definition.Database.Question.QUESTION_FIELD_LESSON_NUMBER, (int) numberOfLesson);
                                    }
                                }
                            });
                        }
                    }).removeField(Definition.Database.Question.QUESTION_FIELD_LESSON_ID);

            /**
             * Process table Grammar.
             */
            RealmObjectSchema grammarSchema = schema.get(Definition.Database.Grammar.GRAMMAR_NAME_TABLE);

            // At version 2 remove field lesson_id inside table Grammar.
            grammarSchema.addField(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_NUMBER, Integer.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(final DynamicRealmObject objGrammar) {
                            // Tiến hành xét giá trị cho trường lesson_number thêm vào bằng việc query con lesson có id trùng với trường lesson_id của đối tượng
                            // sau đó remove trường lesson_id của đối tượng đi.
                            final long lesson_id = objGrammar.getLong(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_ID);

                            lessonSchema.transform(new RealmObjectSchema.Function() {
                                @Override
                                public void apply(final DynamicRealmObject objLesson) {
                                    long idOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_ID);
                                    long numberOfLesson = objLesson.getLong(Definition.Database.Lesson.LESSON_FIELD_NUMBER);

                                    if (lesson_id == idOfLesson) {
                                        objGrammar.setInt(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_NUMBER, (int) numberOfLesson);
                                    }
                                }
                            });
                        }
                    }).removeField(Definition.Database.Grammar.GRAMMAR_FIELD_LESSON_ID);
        }

        if(oldVersion < 4){
            /**
             * Process table Ranking.
             */
            schema.create(Definition.Database.Ranking.RANKING_NAME_TABLE)
                    .addField(Definition.Database.Ranking.RANKING_FIELD_LESSON_NUMBER, int.class)
                    .addField(Definition.Database.Ranking.RANKING_LEVEL, int.class)
                    .addField(Definition.Database.Ranking.RANKING_CATEGORY, int.class)
                    .addField(Definition.Database.Ranking.RANKING_LESSON_TYPE, int.class)
                    .addField(Definition.Database.Ranking.RANKING_QUESTION_TYPE, int.class)
                    .addField(Definition.Database.Ranking.RANKING_ARMORIAL, int.class)
                    .addField(Definition.Database.Ranking.RANKING_TIME, String.class);
        }
    }

    /**
     * Cần override để tránh lỗi crash khi login.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return DatabaseMigration.class.hashCode();
    }

    /**
     * Cần override để tránh lỗi crash khi login.
     *
     * @param object The object.
     * @return The equal result.
     */
    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof DatabaseMigration;
    }
}
