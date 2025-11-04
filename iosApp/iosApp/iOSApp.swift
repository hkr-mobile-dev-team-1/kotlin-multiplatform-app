import SwiftUI
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // connects shared Compose content
        Main_iosViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}