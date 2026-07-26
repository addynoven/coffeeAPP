package com.example.testing1.data

import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.models.CoffeeCategory

object SeedData {

    val coffees = listOf(
        CoffeeEntity(
            id = 1,
            name = "Latte",
            description = "Smooth and creamy",
            category = CoffeeCategory.Latte,
            price = 4.5,
            imageUrl = "https://res.cloudinary.com/demo/image/upload/coffee_sample.jpg"
        ),
        CoffeeEntity(
            id = 2,
            name = "Cappuccino",
            description = "With thick foam",
            category = CoffeeCategory.Cappuccino,
            price = 4.8,
            imageUrl = "https://images.unsplash.com/photo-1534706936160-d5ee67737049?q=80&w=500"
        ),
        CoffeeEntity(
            id = 3,
            name = "Macchiato",
            description = "Espresso with hot water",
            category = CoffeeCategory.Macchiato,
            price = 3.5,
            imageUrl = "https://images.unsplash.com/photo-1485808191679-5f6333c37c8a?q=80&w=500"
        ),
        CoffeeEntity(
            id = 4,
            name = "Espresso",
            description = "Strong and rich",
            category = CoffeeCategory.Espresso,
            price = 3.8,
            imageUrl = "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?q=80&w=500"
        )
    )
}
