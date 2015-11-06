// This is comment line #1
// This is comment line #2

import Foundation

class Test {
    
    // This is comment line #3
    
    var name:String?
    
    func hello() -> String {
        if let name = name {
            return "Hello \(name) !"
        } else {
            return "Who are you ?"
        }
        
    }
    
}