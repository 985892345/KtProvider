import SwiftUI
import ModuleApp

@main
struct iOSApp: App {
    init() {
        ModuleKtProviderInitializer.shared.tryInitKtProvider() // init service
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}