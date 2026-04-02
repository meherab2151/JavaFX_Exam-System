package org.example.demo;

public final class Protocol {

    private Protocol() {}

    public static final int    PORT    = 9_876;
    public static final String HOST    = "127.0.0.1"; 
    public static final String CHARSET = "UTF-8";


    public static final String TEACHER_LOGIN    = "TEACHER_LOGIN";
    public static final String TEACHER_REGISTER = "TEACHER_REGISTER";
    public static final String STUDENT_LOGIN    = "STUDENT_LOGIN";
    public static final String STUDENT_REGISTER = "STUDENT_REGISTER";
    public static final String TEACHERS_LOAD    = "TEACHERS_LOAD";
    public static final String STUDENTS_LOAD    = "STUDENTS_LOAD";

    public static final String QUESTION_SAVE    = "QUESTION_SAVE";
    public static final String QUESTION_UPDATE  = "QUESTION_UPDATE";
    public static final String QUESTION_DELETE  = "QUESTION_DELETE";
    public static final String QUESTIONS_LOAD   = "QUESTIONS_LOAD";

    public static final String EXAM_SAVE        = "EXAM_SAVE";
    public static final String EXAM_DELETE      = "EXAM_DELETE";
    public static final String EXAMS_LOAD       = "EXAMS_LOAD";

    public static final String RESULT_SAVE           = "RESULT_SAVE";
    public static final String RESULTS_LOAD_STUDENT  = "RESULTS_LOAD_STUDENT";
    public static final String RESULTS_LOAD_EXAM     = "RESULTS_LOAD_EXAM";
    public static final String RESULTS_LOAD_ALL      = "RESULTS_LOAD_ALL";
    public static final String RESULT_HAS            = "RESULT_HAS";
    public static final String RESULT_HAS_ANY        = "RESULT_HAS_ANY";
    public static final String RESULT_LOAD_SINGLE    = "RESULT_LOAD_SINGLE";

    public static final String PROGRESS_SAVE    = "PROGRESS_SAVE";
    public static final String PROGRESS_LOAD    = "PROGRESS_LOAD";
    public static final String PROGRESS_FLAGGED = "PROGRESS_FLAGGED";
    public static final String PROGRESS_HAS     = "PROGRESS_HAS";
    public static final String PROGRESS_CLEAR   = "PROGRESS_CLEAR";

    public static final String EXAM_CODE_SAVE   = "EXAM_CODE_SAVE";
    public static final String EXAM_CODE_LOAD   = "EXAM_CODE_LOAD";
    public static final String EXAM_CODE_REMOVE = "EXAM_CODE_REMOVE";

    public static final String ANNOUNCE_SAVE    = "ANNOUNCE_SAVE";
    public static final String ANNOUNCE_DELETE  = "ANNOUNCE_DELETE";
    public static final String ANNOUNCE_LOAD    = "ANNOUNCE_LOAD";
    public static final String ANNOUNCE_PURGE   = "ANNOUNCE_PURGE";

    public static final String PING             = "PING";
    public static final String BYE              = "BYE";


    public static final String PUSH_EXAM_EVENT  = "EXAM_EVENT";
}
