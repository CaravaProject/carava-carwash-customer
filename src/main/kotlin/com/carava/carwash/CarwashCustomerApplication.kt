package com.carava.carwash

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CarwashCustomerApplication

fun main(args: Array<String>) {
	runApplication<CarwashCustomerApplication>(*args)
}
