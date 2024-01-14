import SwiftUI
import ModuleApp

struct ContentView: View {
    var body: some View {
        VStack {
            Text(HelloWorldKt.getHelloWorld())
        }.padding()
    }
}



