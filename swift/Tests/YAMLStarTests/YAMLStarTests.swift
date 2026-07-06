// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

import XCTest

@testable import YAMLStar

final class YAMLStarTests: XCTestCase {
    func testLoadMapping() throws {
        let yaml = try YAMLStar()
        let data = try yaml.load("test: 42")
        let map = try XCTUnwrap(data as? [String: Any])
        XCTAssertEqual(map["test"] as? Int, 42)
    }

    func testLoadPlainYaml() throws {
        let yaml = try YAMLStar()
        let data = try yaml.load("foo: bar")
        let map = try XCTUnwrap(data as? [String: Any])
        XCTAssertEqual(map["foo"] as? String, "bar")
    }

    func testLoadError() throws {
        let yaml = try YAMLStar()
        XCTAssertThrowsError(try yaml.load("key: \"unclosed"))
        XCTAssertNotNil(yaml.error)
    }

    func testLoadMultipleTimes() throws {
        let yaml = try YAMLStar()
        for _ in 0..<2 {
            let data = try yaml.load("test: 42")
            let map = try XCTUnwrap(data as? [String: Any])
            XCTAssertEqual(map["test"] as? Int, 42)
        }
    }
}
