package com.diveroid.lite.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.diveroid.lite.data.Res
import com.google.gson.Gson

class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}

class SqliteUtil {
    companion object {
        @Volatile
        private var instance: SqliteUtil? = null

        @JvmStatic
        fun getInstance(context: Context?): SqliteUtil =
            instance ?: synchronized(this) {
                instance ?: SqliteUtil(context).also {
                    instance = it
                }
            }
    }

    val dbVerPrefName = "universalLiteSqliteVer"

    var context: Context? = null
    var dbHelper: DBHelper
    var database: SQLiteDatabase

    constructor(context: Context?) {
        this.context = context
        dbHelper = DBHelper(context, "universalLite.sqlite", null, 1)
        database = dbHelper.writableDatabase
        //database.disableWriteAheadLogging()//이거 안해주면 에러남!!
    }

    fun runQuery(query: String): String {
        try {
            database.execSQL(query)
            return Gson().toJson(Res(true))
        } catch (e: Exception) {
            return Gson().toJson(Res(false, e.toString()))
        }
    }

    fun runQuerys(querys: List<String>): String {
        try {
            database.beginTransaction()
            for (query in querys) {
                database.execSQL(query)
            }
            database.setTransactionSuccessful();
            database.endTransaction()

            return Gson().toJson(Res(true))
        } catch (e: Exception) {
            return Gson().toJson(Res(false, e.toString()))
        }
    }

    fun selectQuery(query: String): String {

        var rows = mutableListOf<Map<String, String>>()

        try {
            var cursor = database.rawQuery(query, null)
            while (cursor.moveToNext()) {
                var row = mutableMapOf<String, String>()
                for (i in 0..cursor.columnCount - 1) {
                    var column = cursor.getColumnName(i)
                    var value = cursor.getString(i)
                    if (value == null) value = ""

                    row.put(column, value)
                }
                rows.add(row)
            }

            return Gson().toJson(Res(true, "", rows))
        } catch (e: Exception) {
            return Gson().toJson(Res(false, e.toString()))
        }
    }

    fun createTable() {
        val curVerStr = PrefUtil.getInstance(context).getString(dbVerPrefName, "")
        var curVer = 0
        if (curVerStr.length > 0) {
            curVer = curVerStr.toInt()
        }

        if (curVer == 0) {
            createTableVersion1()
            alterTableVersion2()
            alterTableVersion3()
        } else if (curVer == 1) {
            alterTableVersion2()
            alterTableVersion3()
        } else if (curVer == 2) {
            alterTableVersion3()
        }
    }

    fun createTableVersion1() {
        var querys: Array<String> = arrayOf(
            """
           CREATE TABLE IF NOT EXISTS TB_LOG (
                logId INTEGER PRIMARY KEY AUTOINCREMENT, -- 로그아이디
                userId TEXT NOT NULL, -- 유저아이디
                latitude REAL, -- 위도
                longitude REAL, -- 경도
                altitude REAL, -- 고도
                fullLocation TEXT, -- 전체주소: '대한민국 수성구 황금동 213-4'
                majorLocation TEXT, -- 국가,도시: '대한민국, 수성구'
                nationCode TEXT, -- 국가코드: 'KR'
                coverImgFileName TEXT, -- 커버이미지 이름
                startDate TEXT, -- 시작시간: 'yyyyMMddHHmmss'
                endDate TEXT, -- 시작시간: 'yyyyMMddHHmmss'
                deleted INTEGER DEFAULT 0 -- 삭제여부: 삭제시 1
           );
           """,
            """
           CREATE TABLE IF NOT EXISTS TB_MEDIA (
                mediaId INTEGER PRIMARY KEY AUTOINCREMENT,
                logId INTEGER NOT NULL, -- 로그아이디
                fileType TEXT, -- 파일타입: 'IMAGE' 'VIDEO'
                fileName TEXT, -- 파일이름
                thumbName TEXT, -- 썸네일이름
                videoTime TEXT, -- 동영상길이(분:초): '10:15'
                latitude REAL, -- 위도
                longitude REAL, -- 경도
                altitude REAL, -- 고도
                memo TEXT, -- 메모
                createDate TEXT, -- 시작시간: 'yyyyMMddHHmmss'
                deleted INTEGER DEFAULT 0 -- 삭제여부: 삭제시 1
           );
           """,
            """
           CREATE TABLE IF NOT EXISTS TB_COLLECTION (
                collectionId INTEGER PRIMARY KEY AUTOINCREMENT,
                userId TEXT NOT NULL, -- 유저아이디
                collectionName TEXT, -- 컬렉션이름
                favorite INTEGER DEFAULT 0, -- 좋아요: 기본생성컬렉션인 '좋아하는 사진'의 경우 1
                num INTEGER DEFAULT 0, -- 순서: 혹시 필요할지 몰라서..
                createDate TEXT, -- 생성시간: 'yyyyMMddHHmmss'
                deleted INTEGER DEFAULT 0 -- 삭제여부: 삭제시 1
           );
           """,
            """
           CREATE TABLE IF NOT EXISTS TB_COLLECTION_MEDIA (
                collectionMediaId INTEGER PRIMARY KEY AUTOINCREMENT,
                collectionId INTEGER NOT NULL, -- 컬렉션아이디
                mediaId INTEGER, -- 미디어아이디
                num INTEGER DEFAULT 0, -- 순서: 혹시 필요할지 몰라서..
                createDate TEXT, -- 생성시간: 'yyyyMMddHHmmss'
                deleted INTEGER DEFAULT 0 -- 삭제여부: 삭제시 1
           );
           """,
            """
            INSERT INTO TB_COLLECTION VALUES (
                NULL,
                '${AppUtil.getDeviceUuid(context)}',
                '',
                1,
                NULL,
                strftime('%Y%m%d%H%M%S', 'now', 'localtime'),
                NULL
           );
           """
        )

        for (query in querys) {
            try {
                database.execSQL(query)
            } catch (e: Exception) {
                LogUtil.log("onlytree", e.toString())
            }
        }

        PrefUtil.getInstance(context).put(dbVerPrefName, "1")
    }

    fun alterTableVersion2() {
        var querys: Array<String> = arrayOf(
            "ALTER TABLE TB_LOG ADD COLUMN fullLocationEn TEXT" ,
            "ALTER TABLE TB_LOG ADD COLUMN majorLocationEn TEXT"
        )

        for (query in querys) {
            try {
                database.execSQL(query)
            } catch (e: Exception) {
                LogUtil.log("onlytree", e.toString())
            }
        }

        PrefUtil.getInstance(context).put(dbVerPrefName, "2")
    }

    fun alterTableVersion3() {
        var querys: Array<String> = arrayOf(
            "ALTER TABLE TB_MEDIA ADD COLUMN filterd INTEGER DEFAULT 0"
        )

        for (query in querys) {
            try {
                database.execSQL(query)
            } catch (e: Exception) {
                LogUtil.log("onlytree", e.toString())
            }
        }

        PrefUtil.getInstance(context).put(dbVerPrefName, "3")
    }

    fun dropAllTable() {
        var querys: Array<String> = arrayOf(
            """
           DROP TABLE IF EXISTS TB_LOG;
           """,
            """
           DROP TABLE IF EXISTS TB_MEDIA;
           """,
            """
           DROP TABLE IF EXISTS TB_COLLECTION;
           """,
            """
           DROP TABLE IF EXISTS TB_COLLECTION_MEDIA;
           """
        )

        for (query in querys) {
            database.execSQL(query)
        }
    }
}