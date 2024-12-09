package com.example.timer

// BIBLIOTECAS
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.CountDownTimer // BIBLIOTECA PARA O TEMPORIZADOR
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import java.util.Calendar
import android.app.TimePickerDialog
import android.content.Intent
import android.media.MediaPlayer
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

// DESENVOLVEDORES
/*
* BENTO JUNIOR SUZART GOMES -> RESPONSÁVEL PELO BACK-END, DESENVOLVIMENTO DO CÓDIGO FONTE
* RICARDO KOITI MATSUSHITA -> RESPONSÁVEL PELA CRIAÇÃO DO DESIGN E IMPLEMENTAÇÃO DO FRONT-END
* JONATAS RUBENS ALVES DOS SANTOS -> RESPONSÁVEL PELA CRIAÇÃO DO DESIGN E NA DOCUMENTAÇÃO DA APLICAÇÃO
* PROJETO 01 - TIMER GALINÁNCEO
 */

/*CONSULTAS
* https://developer.android.com/reference/kotlin/android/os/CountDownTimer
* https://developer.android.com/reference/kotlin/android/widget/ProgressBar
* https://pt.stackoverflow.com/questions/85349/progressbar-n%C3%A3o-muda-de-cor
* https://developer.android.com/reference/kotlin/android/widget/ProgressBar
*
*
* */
// CLASSE PRINCIPAL DO PROGRAMA
// DESENVOLVIMENTO DO TIMER (TEMPORIZADOR)
class MainActivity : AppCompatActivity() {
    // INTERLIGANDO OS COMPONENTES
    // DECLARAÇÃO DAS VARIÁVEIS QUE SERÃO INCIALIZADAS TARDIAMENTE
    lateinit var timer: TextView
    lateinit var progressBar: ProgressBar
    lateinit var start_btn: ImageButton
    lateinit var reset_btn: ImageButton
    lateinit var bell: ImageView
    lateinit var clock_time: TextClock
    lateinit var mediaPlayer: MediaPlayer
    lateinit var create_btn: Button
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // BUSCANDO OS ELEMENTOS DA INTERFACE
        // ATRIBUINDO OS COMPONENTES DECLARADOS NO .xml
        start_btn = findViewById(R.id.start_btn)
        reset_btn = findViewById(R.id.reset_btn)
        create_btn = findViewById(R.id.create_btn)
        timer = findViewById(R.id.Timer)
        progressBar = findViewById(R.id.progressBar2)
        clock_time = findViewById(R.id.clock_time)
        bell = findViewById(R.id.bell)
        progressBar.max = 100

        // IMPORTANDO O EFEITO SONORO
        //mediaPlayer = MediaPlayer.create(this, R.raw.galinha_sound)

        // DEFININDO A VISIBILIDADE INICIAL DOS COMPONENTES (SINO E O RELÓGIO) COMO GONE
        bell.visibility = View.GONE
        clock_time.visibility = View.GONE

        create_btn.setOnClickListener { // SEGUNDA TELA PARA O HISTÓRICO DE TIMERS CRIADOS
            val intent = Intent(this, TelaRecentTimers::class.java)
            startActivity(intent)
        }

        /************* INTEGRAÇÃO FIREBASE ******************/
        databaseReference = FirebaseDatabase.getInstance().getReference("Timers")

    }

    // DECLARAÇÃO DE VARIÁVEIS
    private var countDownTimer: CountDownTimer? =
        null // CountDownTmer é uma classe do Android utilizada para criar contagens regressivas
    private var isTimerRunning = false // validação se o tempo está operacional
    private var totalDuration: Long = 0 // variável para armazenar a duração total do tempo
    private var timeRemaining: Long = 0 // variável para armazenar o tempo restante

//    INTEGRAÇÃO FIREBASE -> ADICIONAR O TEMPO NO BANCO DE DADOS

    private fun SalvarTempoFirebase(nome: String, timerValue: String) {
        val id = databaseReference.push().key ?: return
        val timerData = TimerData(id, nome, timerValue) // DADOS DA DATA CLASS

        databaseReference.child(id).setValue(timerData)
            .addOnSuccessListener {
                Toast.makeText(this, "Timer salvo com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar o timer: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Método Start recebe o tempo informado pelo usuário fun showTimePicker OSB: o tempo é em milisegundos
    @SuppressLint("DefaultLocale")
    fun startTimer(duration: Long) {

        if (totalDuration == 0L) { //
            totalDuration = duration // armazena a duração total apenas na primeira execução
            // INTEGRAÇÃO FIREBASE
            // SALVANDO OS DADOS NO BANCO DE DADOS "Timers"

            val tempoSelecionado = String.format(
                "%02d:%02d:%02d",
                totalDuration / 3600000,
                (totalDuration / 60000) % 60,
                (totalDuration / 1000) % 60
            )
            mostrarDialogoNomedoTimer(tempoSelecionado)

        }
        timeRemaining = duration // armazena o tempo restante
        if (isTimerRunning) return // retorna caso o cronômetro seja reinicializado antes de ser pausado
        isTimerRunning = true


        // CÁLCULO DO TEMPO FINAL -> O horário em que o cronômetro terminará
        val endTime = System.currentTimeMillis() + timeRemaining

        // Os componentes são tornados visíveis na interface
        bell.visibility = View.VISIBLE
        clock_time.visibility = View.VISIBLE

        // Operação do Cronometro (CountDownTimer)
        // criação do objeto da classe CountDownTimer
        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            // CONVERSÃO DO TEMPO DE MILISEGUNDOS PARA O PADRÃO "00:00:00"
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished // Atualiza o tempo restante

                val seg = (millisUntilFinished / 1000) % 60
                val min = (millisUntilFinished / 1000) / 60 % 60
                val hr = (millisUntilFinished / 1000) / 3600
                val format = String.format("%02d:%02d:%02d", hr, min, seg)
                timer.text = format

                // Atualizando o TextClock com o horário de término
                var Hora_Final =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTime))
                clock_time.text = Hora_Final // atualiza o relógio

                // Atualizando a barra de progresso a medida do decorrer do tempo restante
                val percentComplete =
                    ((totalDuration - millisUntilFinished) * 100 / totalDuration).toInt()
                progressBar.progress = percentComplete
            }

            // função finish é padrão do countDownTimer
            // Ela é invocada quando o cronômetro encerra sua contagem
            override fun onFinish() {
                timer.setText("Done!")
                isTimerRunning = false
                start_btn.setImageResource(R.drawable.startbutton)
                progressBar.progress = 100
                // Tocar o som do alarme
                mediaPlayer.start()
            }
        }.start()

        // start.text = "Pause" start_btn -> pause_btn.ico
        start_btn.setImageResource(R.drawable.pausebutton)

    }

    // Método para retomar o tempo do cronometro (BUTTON RESUME) e alterar o nome do button pause
    private fun resumeTimer() {
        if (timeRemaining > 0) {
            startTimer(timeRemaining)
            start_btn.setImageResource(R.drawable.resumebutton)
        }
    }

    // Método para pausar o cronometro (BUTTON PAUSE) e alterar o nome do button start
    private fun pauseTimer() {
        countDownTimer?.cancel() // Cancela o tempo atual
        isTimerRunning = false // Indica que o timer não está mais em execução
        start_btn.setImageResource(R.drawable.pausebutton)
        // Alterar o "nome" do botão para pause

    }

    // Método Mostrar seletor de tempo
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Criando o TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Converta o tempo selecionado para milissegundos
            val totalMillis: Long = (selectedHour * 3600 + selectedMinute * 60) * 1000L
            startTimer(totalMillis) // duration : long
        }, hour, minute, true)

        timePickerDialog.show()
    }

    // Método start -> onClick do imageButton "start_btn"
    // Este método implementa condições para algumas situações
    /*
    * 1. Caso o tempo não fora iniciado ele chamará o método showTimePicker() -> mostra ao usuário a opção de esolher o tempo
    * 2. Se o tempo estiver percorrendo, o botão é atualizado para "Pause" logo a função pauseTimer() é invocada
    * 3. Caso contrário, o botão "Resume" surge com a invocação da função resumeTimer()
    * 3.1 A hora final é atualizada e apresentada novamente, pois o cronometro retoma sua contagem regressiva
    * */
    fun onStartButtonClick(view: View) { // Método onClick start_btn
        when {
            !isTimerRunning && timeRemaining == 0L -> {
                // Caso o temporizador não esteja em execução e o tempo restante for zero, mostre o TimePicker
                showTimePicker() // permitir que usuário possa escolher o tempo
            }

            isTimerRunning -> {
                // Se o temporizador estiver em execução, pause-o
                pauseTimer()
            }

            else -> {
                // Caso contrário, retome o temporizador
                resumeTimer()
                bell.visibility = View.VISIBLE
                clock_time.visibility = View.VISIBLE
            }
        }
    }

    // Método Reset
    fun resetTimer(view: View) {
        countDownTimer?.cancel()
        timer.text = "00:00:00"
        progressBar.progress = 0
        isTimerRunning = false
        start_btn.setImageResource(R.drawable.startbutton)
        timeRemaining = 0
        totalDuration = 0
        bell.visibility = View.GONE
        clock_time.visibility = View.GONE
    }

    // Método para liberar o recurso do MediaPlayer
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
    // MÉTODO PARA MOSTRAR UM ALERTA PERGUNTANDO SE O USUÁRIO DESEJA COLOCAR UM NOME PARA O TIMER
    fun mostrarDialogoNomedoTimer(tempoSelecionado: String) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        // Diálogo de alerta - mostrar se o usuário deseja denominar um nome para o timer
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Digite o nome do timer")
        builder.setMessage("Insira um nome para o seu timer: ")

        // Define o EditText como o conteúdo do AlertDialog
        builder.setView(input)

        builder.setPositiveButton("Iniciar") {dialog, _ ->
            val nomeTimer = input.text.toString()
            if (nomeTimer.isNotEmpty()) {
                val nomeDoTimer = "$nomeTimer"
                SalvarTempoFirebase(nomeDoTimer, tempoSelecionado)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor, insiria um nome para o timer", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}