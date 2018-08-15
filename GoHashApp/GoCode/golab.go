package golabpac

import (
		"crypto/sha256"
		"golang.org/x/crypto/sha3"
		"golang.org/x/crypto/ripemd160"
		"golang.org/x/crypto/blake2s"
	//	"fmt"
		"encoding/hex"
)


func HashString(hashType string,hashThisText string) string {
	
	switch hashType {
	case "SHA256":
		sum:= sha256.Sum256([]byte(hashThisText))
		return hex.EncodeToString(sum[:])
	case "SHA3-256":
		sum:= sha3.Sum256([]byte(hashThisText))
		return hex.EncodeToString(sum[:])
	case "RIPEMD160":
		sum:= ripemd160.New()
		sum.Write([]byte(hashThisText))
		hash:=sum.Sum(nil)
		return hex.EncodeToString(hash[:])
	case "BLAKE2s-256":
		sum:= blake2s.Sum256([]byte(hashThisText))
		return hex.EncodeToString(sum[:])	
	}

	return "NO SUCH HASH TYPE"
}

func main() {
  //  fmt.Println("This is SHA256 hash = ",hashString("SHA256","Lets hash this sentence!"))
  //  fmt.Println("This is SHA3-256 hash = ",hashString("SHA3-256","Lets hash this sentence!"))
  //  fmt.Println("This is RIPEMD160 hash = ",hashString("RIPEMD160","Lets hash this sentence!"))
  //  fmt.Println("This is BLAKE2s-256 hash = ",hashString("BLAKE2s-256","Lets hash this sentence!"))
}
