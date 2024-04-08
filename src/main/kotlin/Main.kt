package io.github.xypercode

import net.aksingh.owmjapis.core.OWM
import org.eclipse.jface.dialogs.IInputValidator
import org.eclipse.jface.dialogs.InputDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.round
import kotlin.system.exitProcess

val display = Display()
val apiKey = run {
    if (!Files.exists(Paths.get("owm_api_key.txt"))) {
        val shell = Shell(display)

        shell.visible = false
        shell.pack()

        val errorMessage = MessageBox(shell, SWT.ICON_QUESTION or SWT.YES or SWT.NO)
        errorMessage.text = "OWM API Key"
        errorMessage.message = "OWM API key not found\nDo you want to define it now?"
        val result = errorMessage.open()

        if (result == SWT.YES) {
            val dialog = InputDialog(shell, "OWM API key", "OWM API key", "") {
                if (it.length < 32) "Key must be at least 32 characters long" else null
            }

            val open = dialog.open()

            if (open == InputDialog.OK) {
                Files.writeString(Paths.get("owm_api_key.txt"), dialog.value)
            } else {
                display.dispose()
                exitProcess(1)
            }
        }
    }
    Files.readString(Paths.get("owm_api_key.txt")).trim()
}
val owm = OWM(apiKey)

fun main() {
    WeatherApp()
}

class WeatherApp {
    private var podcastWeather: Label
    private var windLbl: Label
    private var humidityLbl: Label
    private var tempLbl: Label
    private var shell = Shell(display)

    init {
        shell.size = Point(300, 300)
        shell.minimumSize = Point(300, 300)
        shell.maximumSize = Point(300, 300)
        shell.text = "Weather App"

        shell.layout = GridLayout(1, false)

        val weather = owm.currentWeatherByCityName("Amsterdam", OWM.Country.NETHERLANDS)
        tempLbl = Label(shell, SWT.NONE)
        tempLbl.text = round(weather.mainData?.temp?.minus(273.15) ?: 0.0).toString() + " °C"
        tempLbl.font = Font(display, "Arial", 40, SWT.NORMAL)

        humidityLbl = Label(shell, SWT.NONE)
        humidityLbl.text = "Humidity: ${weather.mainData?.humidity.toString()} %"

        windLbl = Label(shell, SWT.NONE)
        windLbl.text = "Wind: ${weather.windData?.speed.toString()} m/s"

        podcastWeather = Label(shell, SWT.NONE)
        podcastWeather.text = weather.weatherList?.get(0)?.mainInfo ?: "N/A"

        display.timerExec(5000) {
            val currentWeather = owm.currentWeatherByCityName("Amsterdam", OWM.Country.NETHERLANDS)
            tempLbl.text = round(currentWeather.mainData?.temp?.minus(273.15) ?: 0.0).toString() + " °C"
            humidityLbl.text = "Humidity: ${currentWeather.mainData?.humidity.toString()} %"
            windLbl.text = "Wind: ${currentWeather.windData?.speed.toString()} m/s"

            shell.redraw()
            shell.layout(true)
        }

        shell.addDisposeListener {
            display.dispose()
        }

        shell.pack()
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }
}