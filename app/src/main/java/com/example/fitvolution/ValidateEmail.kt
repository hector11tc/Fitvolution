package com.example.fitvolution

import java.util.regex.Matcher
import java.util.regex.Pattern


class ValidateEmail {
    companion object{
        var pat: Pattern?= null
        var mat: Matcher?= null

        fun isEmail(email:String): Boolean{
            pat = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
            mat = pat!!.matcher(email)
            return mat!!.find()
        }
    }
}




