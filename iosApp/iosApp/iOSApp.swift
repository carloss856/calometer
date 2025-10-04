import SwiftUI
import Shared

@main
struct IOSApp: App {
    init() {
        let catalog = InMemoryFoodCatalogProvider()
        catalog.seed(items: [
            SharedFoodItem(
                id: SharedId(raw: "apple"),
                name: "Apple",
                brand: nil,
                defaultServing: SharedServing(amount: 182, unit: "g"),
                perServing: SharedNutrients(kcal: 95, protein: 0.5, carbs: 25, fat: 0.3)
            )
        ])
        SharedIosInitializerKt.initializeIosDependencies(catalogProvider: catalog)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
