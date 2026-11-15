import Foundation
import AVFoundation

// Lossless concatenation of Android screenrecord segments; no third-party runtime is needed on macOS.
guard CommandLine.arguments.count >= 4 else { fatalError("Usage: swift join_recordings.swift output.mp4 part1.mp4 part2.mp4 ...") }
let output = URL(fileURLWithPath: CommandLine.arguments[1])
let composition = AVMutableComposition()
let track = composition.addMutableTrack(withMediaType: .video, preferredTrackID: kCMPersistentTrackID_Invalid)!
var cursor = CMTime.zero
for path in CommandLine.arguments.dropFirst(2) {
    let asset = AVURLAsset(url: URL(fileURLWithPath: path))
    guard let source = asset.tracks(withMediaType: .video).first else { fatalError("No video track: \(path)") }
    track.preferredTransform = source.preferredTransform
    try track.insertTimeRange(CMTimeRange(start: .zero, duration: asset.duration), of: source, at: cursor)
    cursor = CMTimeAdd(cursor, asset.duration)
}
try? FileManager.default.removeItem(at: output)
let export = AVAssetExportSession(asset: composition, presetName: AVAssetExportPresetPassthrough)!
export.outputURL = output
export.outputFileType = .mp4
export.exportAsynchronously {}
while export.status == .waiting || export.status == .exporting || export.status == .unknown {
    RunLoop.current.run(until: Date().addingTimeInterval(0.1))
}
guard export.status == .completed else { fatalError("Video export failed: \(String(describing: export.error))") }
print("Joined \(CMTimeGetSeconds(cursor)) seconds: \(output.path)")
