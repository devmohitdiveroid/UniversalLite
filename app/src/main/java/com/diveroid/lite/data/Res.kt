package com.diveroid.lite.data

class Res {
    var result: Boolean = true
    var error: String = ""
    var data: Any = ""
    constructor(result: Boolean) {
        this.result = result
    }
    constructor(result: Boolean, error: String) {
        this.result = result
        this.error = error
    }
    constructor(result: Boolean, error: String, data: Any) {
        this.result = result
        this.error = error
        this.data = data
    }
}