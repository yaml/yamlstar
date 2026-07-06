// swift-tools-version:5.9

import PackageDescription

let package = Package(
    name: "YAMLStar",
    products: [
        .library(name: "YAMLStar", targets: ["YAMLStar"])
    ],
    targets: [
        .target(name: "YAMLStar"),
        .testTarget(
            name: "YAMLStarTests",
            dependencies: ["YAMLStar"]
        ),
    ]
)
