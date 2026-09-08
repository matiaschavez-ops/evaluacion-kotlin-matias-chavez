import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

sealed class EstadoPago {
    object Procesando : EstadoPago()
    data class Aprobado(val total: Double) : EstadoPago()
    data class Rechazado(val mensaje: String) : EstadoPago()
}

open class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    var stock: Int
) {
    init {
        require(precio >= 0) { "El precio no puede ser negativo" }
        require(stock >= 0) { "El stock no puede ser negativo" }
    }

    open fun tipo(): String = "Producto"
}

class ProductoFisico(id: Int, nombre: String, precio: Double, stock: Int) :
    Producto(id, nombre, precio, stock) {
    override fun tipo(): String = "Fisico"
}

class ProductoDigital(id: Int, nombre: String, precio: Double, stock: Int) :
    Producto(id, nombre, precio, stock) {
    override fun tipo(): String = "Digital"
}

fun mostrarProductos(productos: List<Producto>) {
    println("\n=== PRODUCTOS ===")
    productos.forEach { producto ->
        println("${producto.id}. ${producto.nombre} | ${producto.tipo()} | $${producto.precio} | Stock: ${producto.stock}")
    }
}

suspend fun procesarPago(total: Double): EstadoPago {
    println("\nProcesando pago...")
    delay(800)
    return if (total > 0) {
        EstadoPago.Aprobado(total)
    } else {
        EstadoPago.Rechazado("El total debe ser mayor que cero")
    }
}

fun main(_args: Array<String>) {
    runBlocking {
        // La lista contiene objetos de las dos clases hijas.
        val productos: List<Producto> = listOf(
            ProductoFisico(1, "Teclado", 35_990.0, 8),
            ProductoFisico(2, "Audifonos", 24_990.0, 12),
            ProductoDigital(3, "Curso Kotlin", 49_990.0, 100),
            ProductoDigital(4, "Editor", 79_990.0, 0)
        )

        mostrarProductos(productos)

        // filter, map y sumOf son funciones de orden superior.
        val disponibles = productos.filter { it.stock > 0 }
        val nombres = disponibles.map { it.nombre }
        println("\nDisponibles: ${nombres.joinToString()}")

        val compra = listOf(productos[0], productos[2])
        val subtotal = compra.sumOf { it.precio }
        val descuento = if (subtotal >= 50_000) subtotal * 0.10 else 0.0
        val envio = if (subtotal >= 80_000) 0.0 else 4_990.0
        val total = subtotal - descuento + envio

        try {
            require(compra.isNotEmpty()) { "La compra esta vacia" }
            require(compra.all { it.stock > 0 }) { "Hay productos sin stock" }
            compra.forEach { it.stock -= 1 }

            println("\n=== RESUMEN ===")
            compra.forEach { producto -> println("${producto.nombre}: $${producto.precio}") }
            println("Subtotal: $$subtotal")
            println("Descuento: $$descuento")
            println("Envio: $$envio")
            println("Total: $$total")

            val estado = procesarPago(total)
            when (estado) {
                EstadoPago.Procesando -> println("El pago sigue procesando")
                is EstadoPago.Aprobado -> println("Pago aprobado: $${estado.total}")
                is EstadoPago.Rechazado -> println("Pago rechazado: ${estado.mensaje}")
            }
        } catch (error: IllegalArgumentException) {
            println("Error controlado: ${error.message}")
        }

        // Comprueba que un dato invalido no cierre la aplicacion.
        try {
            ProductoFisico(99, "Producto invalido", -1.0, 1)
        } catch (error: IllegalArgumentException) {
            println("Dato rechazado: ${error.message}")
        }
    }
}
