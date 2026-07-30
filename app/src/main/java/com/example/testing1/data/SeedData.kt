package com.example.testing1.data

import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.models.CoffeeCategory

object SeedData {

    val coffees = listOf(
        CoffeeEntity(
            id = "7e655541-7783-4df8-9606-2bf77cd36404",
            name = "Latte",
            description = "Smooth and creamy",
            category = CoffeeCategory.Latte,
            price = 4.5,
            imageUrl = "https://res.cloudinary.com/dzao8h1ay/image/upload/v1785076478/coffee_1.png"
        ),
        CoffeeEntity(
            id = "257cd366-7e9a-4a82-a1cb-dc05d259a172",
            name = "Cappuccino",
            description = "With thick foam",
            category = CoffeeCategory.Cappuccino,
            price = 4.8,
            imageUrl = "https://res.cloudinary.com/dzao8h1ay/image/upload/v1785076481/coffee_2.png"
        ),
        CoffeeEntity(
            id = "493c88bd-b071-4b4e-b78a-5cee79e7f1b3",
            name = "Macchiato",
            description = "Espresso with hot water",
            category = CoffeeCategory.Macchiato,
            price = 3.5,
            imageUrl = "https://res.cloudinary.com/dzao8h1ay/image/upload/v1785076484/coffee_3.png"
        ),
        CoffeeEntity(
            id = "43978ed9-718e-4c92-b486-ef9c56ad7285",
            name = "Espresso",
            description = "Strong and rich",
            category = CoffeeCategory.Espresso,
            price = 3.8,
            imageUrl = "https://res.cloudinary.com/dzao8h1ay/image/upload/v1785076487/coffee_4.png"
        ),
        CoffeeEntity(
            id = "0c9cf848-fbd5-443b-aeb6-52ea8769646a",
            name = "Flat White",
            description = "Rich and velvety",
            category = CoffeeCategory.Latte,
            price = 4.2,
            imageUrl = "https://res.cloudinary.com/dzao8h1ay/image/upload/v1785076490/coffee_5.png"
        )
    )
}
