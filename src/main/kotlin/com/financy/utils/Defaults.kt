package com.financy.utils

import com.financy.model.CategoryType

data class DefaultCategoryData(
   val name: String,
   val type: CategoryType,
   val children: Array<DefaultCategoryData>? = null,
)

val defaultCategoriesData = arrayOf<DefaultCategoryData>(
  DefaultCategoryData(
     name = "Unsorted",
     type = CategoryType.Income
  ),
  DefaultCategoryData(
     name = "Salary",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Deposit",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Part-time Job",
     type = CategoryType.Income,
  ),
  DefaultCategoryData(
     name = "Investment",
     type = CategoryType.Income,
  ),

  DefaultCategoryData(
     name = "Unsorted",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Transport",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Car",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Use",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Service",
         type = CategoryType.Outcome,
      ),
    ),
  ),
  DefaultCategoryData(
     name = "Communication",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Mobile phone",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "WiFi",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Food",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Healthy",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Medicine",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Cosmetic",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Diner",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Fast food",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Restaurant",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      )
    )
  ),
  DefaultCategoryData(
     name = "Clothes",
     type = CategoryType.Outcome,
  ),
  DefaultCategoryData(
     name = "Home",
     type = CategoryType.Outcome,
     children = arrayOf(
      DefaultCategoryData(
         name = "Unsorted",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Rent",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Bills",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Chemicals",
         type = CategoryType.Outcome,
      ),
      DefaultCategoryData(
         name = "Comfort",
         type = CategoryType.Outcome,
      )
    )
  )
)