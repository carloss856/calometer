import SwiftUI
import Shared

struct ContentView: View {
    @State private var input: String = ""
    @State private var parsed: [SharedParsedLine] = []
    @State private var note: String = ""
    private let store: NutritionStore = SharedKoinAccessKt.getKoin().get(objCClass: NutritionStore.self) as! NutritionStore

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Registrar comida")) {
                    TextField("Describe tu comida", text: $input, axis: .vertical)
                        .lineLimit(3...6)
                    TextField("Nota opcional", text: $note)
                    Button("Analizar con IA") {
                        store.onInputChanged(text: input)
                        store.parse()
                        parsed = store.state.value.parsedLines
                    }
                }
                Section(header: Text("Resultado")) {
                    ForEach(parsed, id: \.raw) { line in
                        VStack(alignment: .leading) {
                            Text(line.foodName ?? line.raw)
                            if let nutrients = line.computed {
                                Text("≈ \(Int(nutrients.kcal)) kcal")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    Button("Confirmar y guardar") {
                        store.confirmAndSave(note: note.isEmpty ? nil : note)
                        input = ""
                        note = ""
                        parsed = []
                    }
                }
            }
            .navigationTitle("Calometer")
        }
    }
}
